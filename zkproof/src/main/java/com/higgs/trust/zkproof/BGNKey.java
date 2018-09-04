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

	public BGNKey(int bits){
		SecureRandom rng = new SecureRandom();
		TypeA1CurveGenerator a1 = new TypeA1CurveGenerator(rng, 2, bits); // Requires
		// 2
		// prime
		// numbers.
		param = (PropertiesParameters) a1.generate();
		TypeA1Pairing pairing = new TypeA1Pairing(param);

		map = pairing;

		n = param.getBigInteger("n"); // Must extract the prime numbers for
		// both keys.

		f = pairing.getG1();
		P = f.newRandomElement();
		P = P.mul(param.getBigInteger("l"));
		Q = f.newElement();
		Q = Q.set(P);
		Q = Q.mul(param.getBigInteger("n0"));//random r
	}

	public BGNKey(String key){
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

	public Element doPairing(Element A, Element B) {
		return map.pairing(A, B);
	}

	public Element getP() {
		return this.P;
	}

	public Element getQ() {
		return this.Q;
	}

	public BigInteger getN() {
		return this.n;
	}

	public Field getField() {
		return this.f;
	}

	public PropertiesParameters getParam(){
		return  this.param;
	}

	public String exportPubKey(){

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
			return  pubKey.toJSONString();
		} catch (Exception e){
			return null;
		}

	}

	public String exportFullKey(){
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

	public boolean hasFullKey(){
		if (param != null){
			if (param.getBigInteger("n1") != null
					&& param.getBigInteger("n0") != null) {

				return  true;
			}

		}
		return false;

	}

	public boolean hasPubKey(){

		if (P != null && Q != null && n != null){
			if (P.isEqual(f.newZeroElement()) != true
					&& Q.isEqual(f.newZeroElement()) != true
					&& n != BigInteger.ZERO) {
				return true;
			}
		}

		return  false;
	}

}
