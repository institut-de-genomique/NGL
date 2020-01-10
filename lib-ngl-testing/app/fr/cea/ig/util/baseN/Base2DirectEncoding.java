package fr.cea.ig.util.baseN;

class Base2DirectEncoding extends CharMapBaseNEncoding {

	@Override
	public int base() {
		return 2;
	}

	@Override
	public char encodeBaseValue(int value) {
		switch (value) {
		case 0  : return '0';
		case 1  : return '1';
		default : throw new IllegalArgumentException("illegal digit value " + value);
		}
	}

	@Override
	public int decodeBaseValue(char c) {
		switch (c) {
		case '0' : return 0;
		case '1' : return 1;
		default  : throw new IllegalArgumentException("illegal char value '" + c + "'");
		}
	}
	
}
