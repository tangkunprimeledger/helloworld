package com.higgs.trust.zkproof;

import com.alibaba.fastjson.JSONObject;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.parameters.PropertiesParameters;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;


public class BGNKey {
	private TypeA1Pairing map;
	private Element P, Q;
	private BigInteger n;
	private Field f;
	private PropertiesParameters param;
	//static private int subSeqno = 0, subNodeNum = 0;
	private static ThreadLocal<Integer> threadSubSeqno = new ThreadLocal<Integer>();
	private static ThreadLocal<Integer> threadSubNodeNum = new ThreadLocal<Integer>();
	//static private Element subP;
	private static ThreadLocal<Element> threadSubP = new ThreadLocal<Element>();

	static private final int MIN_KEY_NODE_NUM = 4;
	static private final int MIN_SUB_KEY_BIT = 32;


	public BGNKey(TypeA1Pairing pairing, Element gen, Element point,
				  BigInteger order) {
		map = pairing;
		P = gen.set(gen);
		Q = point.set(point);
		n = order;
		f = pairing.getG1();
	}

	BGNKey(int bits){
		SecureRandom rng = new SecureRandom();
		TypeA1CurveGenerator a1 = new TypeA1CurveGenerator(rng, 2, bits); // Requires
		// 2
		// prime
		// numbers.
		param = (PropertiesParameters) a1.generate();
		TypeA1Pairing pairing = new TypeA1Pairing(param);

		map = pairing;

		n = param.getBigInteger("n");// Must extract the prime numbers for
		// both keys.

		f = pairing.getG1();
		P = f.newRandomElement();
		P = P.mul(param.getBigInteger("l"));
		Q = f.newElement();
		Q = Q.set(P);
		Q = Q.mul(param.getBigInteger("n0"));//random r
	}

	BGNKey(String key){
		JSONObject pubKey = JSONObject.parseObject(key);

		String p1 = pubKey.getString("param");
		try {
			param = (PropertiesParameters) SerializerUtil.deserialize(p1);
			TypeA1Pairing pairing = new TypeA1Pairing(param);
			f = pairing.getG1();
			//this.f.setFromString(pubKey.getString("f"));
			P = f.newElement();
			Q = f.newElement();
			this.P.setFromBytes(Base58.decode(pubKey.getString("P")));
			this.Q.setFromBytes(Base58.decode(pubKey.getString("Q")));
			this.n = Base58.decodeToBigInteger(pubKey.getString("n"));
			if(pubKey.getString("nodeNum") != null){
				threadSubNodeNum.set(Integer.valueOf(pubKey.getString("nodeNum")));
				threadSubSeqno.set(1);
				Element newP = f.newElement();
				Element newQ = f.newElement();
				Element tmpP = f.newElement();
				int seqno;
				for (seqno = 1; seqno <= threadSubNodeNum.get(); seqno++){
					if (pubKey.getString("P" + String.valueOf(seqno)) == null) {
						break;
					}
					tmpP.setFromBytes(Base58.decode(pubKey.getString("P" + String.valueOf(seqno))));
					if (seqno % 2 == 0){
						newQ = newQ.add(tmpP);
					}
					if (seqno % 2 == 1){
						newP = newP.add(tmpP);
					}
				}
				if (seqno > threadSubNodeNum.get()){
					if (Base58.encode(P.toBytes()).compareTo(Base58.encode(newP.toBytes())) != 0
							&& Base58.encode(Q.toBytes()).compareTo(Base58.encode(newQ.toBytes())) != 0){
						P = newP;
						Q = newQ;
					} else {
						P = null;
						Q = null;
						f = null;
						n = null;
					}

				}
			}

		} catch (Exception e) {

		}
	}


	Element doPairing(Element A, Element B) {
		return map.pairing(A, B);
	}

	Element getP() {
		return this.P;
	}

	Element getQ() {
		return this.Q;
	}

	BigInteger getN() {
		return this.n;
	}

	Field getField() {
		return this.f;
	}

	PropertiesParameters getParam(){
		return  this.param;
	}

	String exportPubKeyMod(){

		try {
			PairingParameters param1 = (PairingParameters) SerializerUtil.deserialize(SerializerUtil.serialize(param));
			//delete private key
			param1.remove("n0");
			param1.remove("n1");
			BigInteger n1 = new BigInteger(param1.getBigInteger("n").bitLength(),64,new Random()); // Must extract the prime numbers for
			param1.put("n",n1.toString());
			String p1 = SerializerUtil.serialize(param1);
			JSONObject pubKey = new JSONObject();
			pubKey.put("key_type","BGN");
			pubKey.put("param", p1);
			pubKey.put("P",Base58.encode(P.toBytes()));
			pubKey.put("Q",Base58.encode(Q.toBytes()));
			pubKey.put("n",Base58.encode(n1.toByteArray()));
			return  pubKey.toJSONString();
		} catch (Exception e){
			return null;
		}

	}

	String exportPubKey(){

		try {
			PairingParameters param1 = (PairingParameters) SerializerUtil.deserialize(SerializerUtil.serialize(param));
			//delete private key
			param1.remove("n0");
			param1.remove("n1");
			String p1 = SerializerUtil.serialize(param1);
			JSONObject pubKey = new JSONObject();
			pubKey.put("key_type","BGN");
			pubKey.put("param", p1);
			pubKey.put("P",Base58.encode(P.toBytes()));
			pubKey.put("Q",Base58.encode(Q.toBytes()));
			pubKey.put("n",Base58.encode(n.toByteArray()));
			if (threadSubNodeNum.get() != null){
				pubKey.put("nodeNum",String.valueOf(threadSubNodeNum.get()));
				pubKey.put("P"+String.valueOf(threadSubSeqno.get()),Base58.encode(threadSubP.get().toBytes()));
			}
			return  pubKey.toJSONString();
		} catch (Exception e){
			return null;
		}

	}

	static String GenSubKey(String key, int seqno, int nodeNum){
		Element T;
		JSONObject ob = JSONObject.parseObject(key);
		int oldNodeNum = Integer.valueOf(ob.getString("nodeNum") == null ? "-1":ob.getString("nodeNum"));
		BGNKey baseKey = new BGNKey(key);
		T = baseKey.getField().newElement();
		T.setFromBytes(Base58.decode(ob.getString("P")));
		T = T.mul(baseKey.getN());
		if (Base58.encode(T.toBytes()).compareTo(ob.getString("P")) == 0
				&& baseKey.getN().compareTo(BigInteger.ZERO) != 0
				&& seqno <= nodeNum && seqno > 0
				&&((oldNodeNum != -1 && oldNodeNum == nodeNum) || oldNodeNum == -1)
				&& nodeNum >= MIN_KEY_NODE_NUM
				&& baseKey.getN().bitLength()/nodeNum/2 >= MIN_SUB_KEY_BIT){
			Element subP = baseKey.getField().newElement();
			do {
				BigInteger r = new BigInteger(baseKey.getN().bitLength()/nodeNum/2 ,64,new Random());
				subP.set(baseKey.getP());
				subP = subP.mul(r);
				threadSubP.set(subP);
			} while (Base58.encode(subP.toBytes()).compareTo(ob.getString("P")) == 0
					|| Base58.encode(subP.toBytes()).compareTo(ob.getString("Q")) == 0);
			threadSubSeqno.set(seqno);
			ob.put("P"+String.valueOf(threadSubSeqno.get()),Base58.encode(subP.toBytes()));
			threadSubNodeNum.set(nodeNum);

			if (oldNodeNum == -1){
				ob.put("nodeNum",String.valueOf(threadSubNodeNum.get()));
			}
			return ob.toJSONString();
		}
		return null;
	}

	public static String MergeKey(String key1, String key2) {
		JSONObject ob1 = JSONObject.parseObject(key1);
		JSONObject ob2 = JSONObject.parseObject(key2);

		if (ob1.getString("param").compareTo(ob2.getString("param")) == 0
				&& ob1.getString("P").compareTo(ob2.getString("P")) == 0
				&& ob1.getString("Q").compareTo(ob2.getString("Q")) == 0
				&& ob1.getString("n").compareTo(ob2.getString("n")) == 0){

			int NodeNum1 = Integer.valueOf(ob1.getString("nodeNum") == null ? "-1":ob1.getString("nodeNum"));
			int NodeNum2 = Integer.valueOf(ob2.getString("nodeNum") == null ? "-1":ob2.getString("nodeNum"));
			if (NodeNum1 == NodeNum2 && NodeNum2 != -1) {
				for(int seqno = 1; seqno <= NodeNum2; seqno++){
					if (ob1.getString("P" + String.valueOf(seqno)) == null
							&& ob2.getString("P" + String.valueOf(seqno)) != null){
						ob1.put("P" + String.valueOf(seqno),ob2.getString("P" + String.valueOf(seqno)));
					}
				}
				return  ob1.toJSONString();
			}

		}
		return null;
	}

	public static boolean ContainKey(String fullKey, String subKey){
		JSONObject ob1 = JSONObject.parseObject(fullKey);
		JSONObject ob2 = JSONObject.parseObject(subKey);

		if (ob1.getString("param").compareTo(ob2.getString("param")) == 0
				&& ob1.getString("P").compareTo(ob2.getString("P")) == 0
				&& ob1.getString("Q").compareTo(ob2.getString("Q")) == 0
				&& ob1.getString("n").compareTo(ob2.getString("n")) == 0){

			int NodeNum1 = Integer.valueOf(ob1.getString("nodeNum") == null ? "-1":ob1.getString("nodeNum"));
			int NodeNum2 = Integer.valueOf(ob2.getString("nodeNum") == null ? "-1":ob2.getString("nodeNum"));
			if (NodeNum1 == NodeNum2 && NodeNum2 != -1) {
				for(int seqno = 1; seqno <= NodeNum2; seqno++){
					if (ob2.getString("P" + String.valueOf(seqno)) != null  ){
						if (ob1.getString("P" + String.valueOf(seqno)) != null){
							if(ob2.getString("P" + String.valueOf(seqno)).compareTo(ob1.getString("P" + String.valueOf(seqno))) != 0){
								return false;
							}
						}
						else {
							return false;
						}
					}
				}
				return  true;
			}

		}

		return false;
	}


	String exportFullKey(){
		try {
			String p1 = SerializerUtil.serialize(param);
			JSONObject fullKey = new JSONObject();
			fullKey.put("key_type","BGN");
			fullKey.put("param", p1);
			fullKey.put("P",Base58.encode(P.toBytes()));
			fullKey.put("Q",Base58.encode(Q.toBytes()));
			fullKey.put("n",Base58.encode(n.toByteArray()));
			if (threadSubNodeNum.get() != null){
				fullKey.put("nodeNum",String.valueOf(threadSubNodeNum.get()));
				fullKey.put("P"+String.valueOf(threadSubSeqno.get()),Base58.encode(threadSubP.get().toBytes()));
			}
			return  fullKey.toJSONString();
		} catch (Exception e){
			return null;
		}

	}

	boolean hasFullKey(){
		if (param != null){
			return (param.getBigInteger("n1") != null
					&& param.getBigInteger("n0") != null);

		}
		return false;
	}

	boolean hasPubKey(){
		return (P != null && Q != null && n != null && param != null);
	}

}
