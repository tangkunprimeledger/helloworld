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

	String exportPubKey(){

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

	String exportFullKey(){
		try {
			String p1 = SerializerUtil.serialize(param);
			JSONObject fullKey = new JSONObject();
			fullKey.put("key_type","BGN");
			fullKey.put("param", p1);
			fullKey.put("P",Base58.encode(P.toBytes()));
			fullKey.put("Q",Base58.encode(Q.toBytes()));
			fullKey.put("n",Base58.encode(n.toByteArray()));
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
