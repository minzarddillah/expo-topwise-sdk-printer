package com.cashlez.sdkprintertopwise;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HexUtil {

	public static byte[] hexStringToByte(String data) {
		String hex = data.toUpperCase();
		int len = (hex.length() / 2);
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}
	public static byte[] StringToByte(String str) {
		int len = str.length();
		byte[] result = new byte[len];
		char[] achar = str.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) achar[i];
		}
		return result;
	}
	public static String bcd2str(byte[] bcds) {
		if (bcds == null)
			return "";
		char[] ascii = "0123456789abcdef".toCharArray();
		byte[] temp = new byte[bcds.length * 2];
		for (int i = 0; i < bcds.length; i++) {
			temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
			temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
		}
		StringBuffer res = new StringBuffer();

		for (int i = 0; i < temp.length; i++) {
			res.append(ascii[temp[i]]);
		}
		return res.toString().toUpperCase();
	}

	private static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	public static byte[] byteMerger(byte[] byte1, byte[] byte2) {
		byte[] byte3 = new byte[byte1.length + byte2.length];
		System.arraycopy(byte1, 0, byte3, 0, byte1.length);
		System.arraycopy(byte2, 0, byte3, byte1.length, byte2.length);
		return byte3;
	}

	public static byte[] int2bytes(int num) {
		byte[] b = new byte[4];
		int mask = 0xff;
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (num >>> (24 - i * 8));
		}
		return b;
	}
    //低位在前高位在后
	public static byte[] int2bytesNew(int num) {
		byte[] b = new byte[4];
		int mask = 0xff;
		for (int i = 0; i < 4; i++) {
			b[i] = (byte) (num >>> (i * 8));
		}
		return b;
	}

	public static int bytes2int(byte[] b) {
		// byte[] b=new byte[]{1,2,3,4};
		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 4; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	public static byte[] short2bytes(short num) {
		byte[] targets = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (targets.length - 1 - i) * 8;
			targets[i] = (byte) ((num >>> offset) & 0xff);
		}
		return targets;
	}

	/**
	 * 将长度为2的byte数组转换6位int
	 * 
	 * @param b
	 *            byte[]
	 * @return int
	 * */
	public static int bytes2short(byte[] b) {
		// byte[] b=new byte[]{1,2,3,4};
		int mask = 0xff;
		int temp = 0;
		int res = 0;
		for (int i = 0; i < 2; i++) {
			res <<= 8;
			temp = b[i] & mask;
			res |= temp;
		}
		return res;
	}

	public static String getBinaryStrFromByteArr(byte[] bArr) {
		String result = "";
		for (byte b : bArr) {
			result += getBinaryStrFromByte(b);
		}
		return result;
	}

	public static String getBinaryStrFromByte(byte b) {
		String result = "";
		byte a = b;
        for (int i = 0; i < 8; i++) {
			byte c = a;
			a = (byte) (a >> 1);
			a = (byte) (a << 1);
			if (a == c) {
				result = "0" + result;
			} else {
				result = "1" + result;
			}
			a = (byte) (a >> 1);
		}
		return result;
	}

	public static String getBinaryStrFromByte2(byte b) {
		String result = "";
		byte a = b;
        for (int i = 0; i < 8; i++) {
			result = (a % 2) + result;
			a = (byte) (a >> 1);
		}
		return result;
	}

	public static String getBinaryStrFromByte3(byte b) {
		String result = "";
		byte a = b;
        for (int i = 0; i < 8; i++) {
			result = (a % 2) + result;
			a = (byte) (a / 2);
		}
		return result;
	}

	public static byte[] toByteArray(int iSource, int iArrayLen) {
		byte[] bLocalArr = new byte[iArrayLen];
		for (int i = 0; (i < 4) && (i < iArrayLen); i++) {
			bLocalArr[i] = (byte) (iSource >> 8 * i & 0xFF);

		}
		return bLocalArr;
	}

	public static byte[] xor(byte[] op1, byte[] op2) {
		if (op1.length != op2.length) {
			throw new IllegalArgumentException("op1.length != op2.length");
		}
		byte[] result = new byte[op1.length];
		for (int i = 0; i < op1.length; i++) {
			result[i] = (byte) (op1[i] ^ op2[i]);
		}
		return result;
	}

	public static class TR31D {
		private byte[] mKeyData = null;

		public boolean DecryptPack(String pack, byte[] kbpk) {
			byte[] K2 = new byte[16];
			byte[] KM1 = new byte[16];
			byte[] kbek = new byte[kbpk.length];
			byte[] kbak = new byte[kbpk.length];
			byte[] getMac = null;
			byte[] calMac = null;
			byte[] encryptPackBytes = null;
			byte[] decryptPackBytes = null;
			byte[] macPack = null;
			byte[] tmpBytes = null;
			int i = 0;

			Cmac(kbpk, null, K2);

			//System.exit(0);

			GetKbek(kbpk, K2, kbek);
			GetKbak(kbpk, K2, kbak);

			//System.out.println("kbpk = " + hexToString(kbpk));
			//System.out.println("K2 = " + hexToString(K2));
			//System.out.println("kbek = " + hexToString(kbek));
			//System.out.println("kbak = " + hexToString(kbak));

			getMac = stringToHex(pack.substring(pack.length() - 32));
			encryptPackBytes = stringToHex(pack.substring(16, pack.length() - 32));
			if (encryptPackBytes == null) {
				System.out.println("error1");
				return false;
			}

			decryptPackBytes = DecryptKeyData(kbek, getMac, encryptPackBytes);
			if (decryptPackBytes == null) {
				System.out.println("error2");
				return false;
			}
			if (decryptPackBytes.length < 16 + 16) {
				System.out.println("error3");
				return false;
			}
			i = ((((int) decryptPackBytes[0]) & 0x000000FF) << 8) + (((int) decryptPackBytes[1]) & 0x000000FF);
			if ((i % 8) != 0) {
				System.out.println("error4");
				return false;
			}
			i = i / 8;
			if (i != 16 && i != 24 && i != 32) {
				System.out.println("error5");
				return false;
			}

			Cmac(kbak, KM1, null);

			macPack = new byte[16 + (pack.length() - 16 - 32) / 2];
			System.arraycopy(pack.getBytes(), 0, macPack, 0, 16);
			System.arraycopy(decryptPackBytes, 0, macPack, 16, macPack.length - 16);
			calMac = CalMac(macPack, kbak, KM1);

			if (calMac == null || getMac == null || calMac.length != getMac.length) {
				System.out.println("error6");
				return false;
			}
			for (i = 0; i < calMac.length; i++) {
				if (calMac[i] != getMac[i]) {
					System.out.println("error7");
					return false;
				}
			}

			mKeyData = new byte[i];
			System.arraycopy(decryptPackBytes, 2, mKeyData, 0, i);

			return true;
		}

		public String EncryptToPack(byte[] kbpk, String headStr, byte[] plainKey, byte[] random) {
			//String strTmp = null;
			byte[] kbek = new byte[kbpk.length];
			byte[] kbak = new byte[kbpk.length];
			byte[] K2 = new byte[16];
			byte[] KM1 = new byte[16];
			byte[] calMac = null;
			byte[] tmpPack = null;

			//16 + 2 + keylen + padding + 16
			headStr = headStr.charAt(0) + String.format("%04d", 16 + ((2 + plainKey.length + 15) / 16) * 16 * 2 + 16 * 2) + headStr.substring(5);
			Cmac(kbpk, null, K2);

			GetKbek(kbpk, K2, kbek);
			GetKbak(kbpk, K2, kbak);

			Cmac(kbak, KM1, null);
			tmpPack = new byte[16 + 2 + plainKey.length + 16 - ((16 + plainKey.length + 2) % 16)];
			System.arraycopy(headStr.getBytes(), 0, tmpPack, 0, 16);
			tmpPack[16] = (byte) ((plainKey.length * 8) >> 8);
			tmpPack[17] = (byte) ((plainKey.length * 8) >> 0);
			System.arraycopy(plainKey, 0, tmpPack, 16 + 2, plainKey.length);
			System.arraycopy(random, 0, tmpPack, 16 + 2 + plainKey.length, 16 - ((16 + 2 + plainKey.length) % 16));
			System.out.println("cal mac pack=" + hexToString(tmpPack));
			calMac = CalMac(tmpPack, kbak, KM1);
			tmpPack = new byte[2 + plainKey.length + 16 - ((16 + 2 + plainKey.length) % 16)];
			tmpPack[0] = (byte) ((plainKey.length * 8) >> 8);
			tmpPack[1] = (byte) ((plainKey.length * 8) >> 0);
			System.arraycopy(plainKey, 0, tmpPack, 2, plainKey.length);
			System.arraycopy(random, 0, tmpPack, 2 + plainKey.length, 16 - ((16 + 2 + plainKey.length) % 16));
			tmpPack = EncryptKeyData(kbek, calMac, tmpPack);

			return headStr + (hexToString(tmpPack) + hexToString(calMac)).toUpperCase();
		}

		public byte[] getKeyData() {
			return mKeyData;
		}

		byte[] CalMac(byte[] totalPack, byte[] kbak, byte[] km1) {
			byte[] tmpBytes = null;
			Cipher cipher = null;
			SecretKeySpec secretKeySpec = null;
			IvParameterSpec ivps = null;
			byte[] encryptedBytes = null;

			try {
				cipher = Cipher.getInstance("AES/CBC/NoPadding");

				secretKeySpec = new SecretKeySpec(kbak, "AES");
				ivps = new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

				cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivps);

				encryptedBytes = cipher.doFinal(totalPack, 0, totalPack.length - 16);
				if (encryptedBytes.length < 16) {
					return null;
				}
				tmpBytes = new byte[16];
				System.arraycopy(encryptedBytes, encryptedBytes.length - 16, tmpBytes, 0, 16);
				Xor(tmpBytes, 0, totalPack, totalPack.length - 16, 16);

				cipher = Cipher.getInstance("AES/CBC/NoPadding");

				secretKeySpec = new SecretKeySpec(kbak, "AES");
				ivps = new IvParameterSpec(km1);

				cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivps);

				encryptedBytes = cipher.doFinal(tmpBytes);

				return encryptedBytes;
			} catch (Exception e) {
				return null;
			}
		}

		private byte[] DecryptKeyData(byte[] kbek, byte[] mac, byte[] cipherDataIn) {
			try {
				Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

				SecretKeySpec secretKeySpec = new SecretKeySpec(kbek, "AES");
				IvParameterSpec ivps = new IvParameterSpec(mac);

				cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivps);

				byte[] encryptedBytes = cipher.doFinal(cipherDataIn);

				return encryptedBytes;
			} catch (Exception e) {
				return null;
			}
		}

		private byte[] EncryptKeyData(byte[] kbek, byte[] mac, byte[] plainDataIn) {
			try {
				Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

				SecretKeySpec secretKeySpec = new SecretKeySpec(kbek, "AES");
				IvParameterSpec ivps = new IvParameterSpec(mac);

				cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivps);

				byte[] encryptedBytes = cipher.doFinal(plainDataIn);

				return encryptedBytes;
			} catch (Exception e) {
				return null;
			}
		}

		void GetKbek(byte[] kbpk, byte[] K2, byte[] outKbek) {
			byte[] part1Org16 = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, (byte) 0x80, (byte) 0x80, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00, 0x00, };
			byte[] part1Org24 = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, (byte) 0xC0, (byte) 0x80, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00, 0x00, };
			byte[] part2Org24 = new byte[] { 0x02, 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, (byte) 0xC0, (byte) 0x80, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00, 0x00, };

			byte[] part1Org32 = new byte[] { 0x01, 0x00, 0x00, 0x00, 0x00, 0x04, 0x01, 0x00, (byte) 0x80, 0x00, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00, };
			byte[] part2Org32 = new byte[] { 0x02, 0x00, 0x00, 0x00, 0x00, 0x04, 0x01, 0x00, (byte) 0x80, 0x00, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00, };
			byte[] tmp1 = new byte[16];
			byte[] tmp2 = new byte[16];
			final int kbpkLen = kbpk.length;
			byte[] tmpOut = null;

			System.arraycopy(K2, 0, tmp1, 0, 16);
			if (kbpkLen == 16) {
				Xor(tmp1, part1Org16, 16);
			}
			if (kbpkLen == 24) {
				Xor(tmp1, part1Org24, 16);
			}
			if (kbpkLen == 32) {
				Xor(tmp1, part1Org32, 16);
			}

			tmpOut = encrypt(kbpk, tmp1);
			System.arraycopy(tmpOut, 0, outKbek, 0, 16);
			if (kbpkLen <= 16) {
				return;
			}

			System.arraycopy(K2, 0, tmp1, 0, 16);
			if (kbpkLen == 24) {
				Xor(tmp1, part2Org24, 16);
			}
			if (kbpkLen == 32) {
				Xor(tmp1, part2Org32, 16);
			}
			tmpOut = encrypt(kbpk, tmp1);
			System.arraycopy(tmpOut, 0, outKbek, 16, kbpkLen - 16);

		}

		void GetKbak(byte[] kbpk, byte[] K2, byte[] outKbak) {
			byte[] part1Org16 = new byte[] { 0x01, 0x00, 0x01, 0x00, 0x00, 0x02, 0x00, (byte) 0x80, (byte) 0x80, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00, 0x00 };// "\x01\x00\x01\x00\x00\x02\x00\x80\x80\x00\x00\x00\x00\x00\x00\x00";
			byte[] part1Org24 = new byte[] { 0x01, 0x00, 0x01, 0x00, 0x00, 0x03, 0x00, (byte) 0xC0, (byte) 0x80, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00, 0x00 };// "\x01\x00\x01\x00\x00\x03\x00\xC0\x80\x00\x00\x00\x00\x00\x00\x00";
			byte[] part2Org24 = new byte[] { 0x02, 0x00, 0x01, 0x00, 0x00, 0x03, 0x00, (byte) 0xC0, (byte) 0x80, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00, 0x00 };// "\x02\x00\x01\x00\x00\x03\x00\xC0\x80\x00\x00\x00\x00\x00\x00\x00";
			byte[] part1Org32 = new byte[] { 0x01, 0x00, 0x01, 0x00, 0x00, 0x04, 0x01, 0x00, (byte) 0x80, 0x00, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00 };// "\x01\x00\x01\x00\x00\x04\x01\x00\x80\x00\x00\x00\x00\x00\x00\x00";
			byte[] part2Org32 = new byte[] { 0x02, 0x00, 0x01, 0x00, 0x00, 0x04, 0x01, 0x00, (byte) 0x80, 0x00, 0x00, 0x00,
					0x00, 0x00, 0x00, 0x00, };// "\x02\x00\x01\x00\x00\x04\x01\x00\x80\x00\x00\x00\x00\x00\x00\x00";
			byte[] tmp1 = new byte[16];
			byte[] tmp2 = new byte[16];
			byte[] tmpOut = null;

			final int kbpkLen = kbpk.length;

			System.arraycopy(K2, 0, tmp1, 0, 16);
			if (kbpkLen == 16) {
				Xor(tmp1, part1Org16, 16);
			}
			if (kbpkLen == 24) {
				Xor(tmp1, part1Org24, 16);
			}
			if (kbpkLen == 32) {
				Xor(tmp1, part1Org32, 16);
			}

			tmpOut = encrypt(kbpk, tmp1);
			System.arraycopy(tmpOut, 0, outKbak, 0, 16);

			if (kbpkLen <= 16) {
				return;
			}

			System.arraycopy(K2, 0, tmp1, 0, 16);
			if (kbpkLen == 24) {
				Xor(tmp1, part2Org24, 16);
			}
			if (kbpkLen == 32) {
				Xor(tmp1, part2Org32, 16);
			}
			tmpOut = encrypt(kbpk, tmp1);
			System.arraycopy(tmpOut, 0, outKbak, 16, kbpkLen - 16);
		}


		private void Cmac(byte[] keyIn, byte[] outK1, byte[] outK2) {
			byte[] S = null;
			int MSB0 = 0;

			S = encrypt(keyIn, new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

			if ((S[0] & 0x80) != 0) {
				MSB0 = 1;
			} else {
				MSB0 = 0;
			}

			//System.out.println("1 = " + hexToString(S));
			ShiftLeft1Bits(S, 16);
			//System.out.println("2 = " + hexToString(S));

			if (MSB0 != 0) {
				Xor(S, new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
						0x00, (byte) 0x87 }, 16);
			}

			if (outK1 != null) {
				System.arraycopy(S, 0, outK1, 0, 16);
			}

			if ((S[0] & 0x80) != 0) {
				MSB0 = 1;
			} else {
				MSB0 = 0;
			}

			ShiftLeft1Bits(S, 16);

			if (MSB0 != 0) {
				Xor(S, new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
						0x00, (byte) 0x87 }, 16);
			}

			if (outK2 != null) {
				System.arraycopy(S, 0, outK2, 0, 16);
			}
		}

		void ShiftLeft1Bits(byte[] data, int bytesLen) {
			int i = 0;
			byte keep0 = data[0];

			for (i = 0; i < bytesLen; i++) {
				data[i] = (byte) ((((int) data[i]) & 0xff) << 1);
				if (i + 1 >= bytesLen) {
				/*if ((keep0 & 0x80) != 0) {
					data[i] |= 0x01;
				} else {
					data[i] &= 0xFE;
				}*/
					data[i] &= 0xFE;
				} else {
					if ((data[i + 1] & 0x80) != 0) {
						data[i] |= 0x01;
					} else {
						data[i] &= 0xFE;
					}
				}
			}
		}

		private void Xor(byte[] mainE, byte[] xorToMainE, int eLen) {
			int i = 0;

			for (i = 0; i < eLen; i++) {
				mainE[i] ^= xorToMainE[i];
			}
		}

		private void Xor(byte[] mainE, int mainEOff, byte[] xorToMainE, int xorToMainEOff, int eLen) {
			int i = 0;

			for (i = 0; i < eLen; i++) {
				mainE[i + mainEOff] ^= xorToMainE[i + xorToMainEOff];
			}
		}

		private static byte[] encrypt(byte[] key, byte[] text) {
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

				SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
				;

				cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

				byte[] encryptedBytes = cipher.doFinal(text);

				return encryptedBytes;
			} catch (Exception e) {
				return null;
			}
		}

		private static byte[] decrypt(byte[] key, byte[] text) {
			try {
				Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

				SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
				;

				cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

				byte[] encryptedBytes = cipher.doFinal(text);

				return encryptedBytes;
			} catch (Exception e) {
				return null;
			}
		}

		private static String hexToString(byte[] buf) {
			return hexToString(buf, 0, buf.length);
		}

		private static String hexToString(byte[] buf, int index, int len) {
			String out = new String();
			int j = 0;
			int i = 0;

			for (i = 0; i < len; i++) {
				j = ((int) (buf[i + index])) & 0x000000FF;
				if ((j & 0x000000F0) == 0) {
					out += "0";
				}
				out += Integer.toHexString(j);
			}
			return out;
		}

		private static byte[] stringToHex(String str) {
			byte[] hex = null;
			int i = 0;
			int cnt = 0;

			cnt = 0;
			for (i = 0; i < str.length(); i++) {
				if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
					cnt++;
				} else if (str.charAt(i) >= 'a' && str.charAt(i) <= 'f') {
					cnt++;
				} else if (str.charAt(i) >= 'A' && str.charAt(i) <= 'F') {
					cnt++;
				}
			}

			hex = new byte[(cnt + 1) / 2];
			cnt = 0;
			for (i = 0; i < str.length(); i++) {
				if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
					if ((cnt % 2) == 0) {
						hex[cnt / 2] = (byte) (str.charAt(i) - '0');
						hex[cnt / 2] <<= 4;
					} else {
						hex[cnt / 2] &= 0xF0;
						hex[cnt / 2] |= ((byte) (str.charAt(i) - '0'));
					}
					cnt++;
				} else if (str.charAt(i) >= 'a' && str.charAt(i) <= 'f') {
					if ((cnt % 2) == 0) {
						hex[cnt / 2] = (byte) (str.charAt(i) - 'a' + 0x0A);
						hex[cnt / 2] <<= 4;
					} else {
						hex[cnt / 2] &= 0xF0;
						hex[cnt / 2] |= ((byte) (str.charAt(i) - 'a' + 0x0A));
					}
					cnt++;
				} else if (str.charAt(i) >= 'A' && str.charAt(i) <= 'F') {
					if ((cnt % 2) == 0) {
						hex[cnt / 2] = (byte) (str.charAt(i) - 'A' + 0x0A);
						hex[cnt / 2] <<= 4;
					} else {
						hex[cnt / 2] &= 0xF0;
						hex[cnt / 2] |= ((byte) (str.charAt(i) - 'A' + 0x0A));
					}
					cnt++;
				}
			}

			return hex;
		}

	}
}
