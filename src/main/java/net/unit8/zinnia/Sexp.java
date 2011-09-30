package net.unit8.zinnia;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class Sexp {
	private static enum Stat { CONS, ATOM };
		
	public static class Cell {
		private Stat stat;
		private Cons cons;
		private String atom;
		public boolean isCons() {
			return stat == Stat.CONS;
		}
		public boolean isAtom() {
			return stat == Stat.ATOM;
		}
		
		public void setCdr(Cell cell) {
			stat = Stat.CONS;
			if(cons == null)
				cons = new Cons();
			cons.car = cell;
		}
		
		public void setCar(Cell cell) {
			stat = Stat.CONS;
			if(cons == null)
				cons = new Cons();
			cons.cdr = cell;
		}
		
		public void setAtom(String atom) {
			stat = Stat.ATOM;
			this.atom = atom;
		}
		public Cell getCar()  { return cons.car; }
		public Cell getCdr()  { return cons.cdr; }
		public String getAtom() { return atom; }
		
	}
	
	public static class Cons {
		public Cell car;
		public Cell cdr;
	}
	

	public Cell read(StringCharacterIterator sexp) throws IOException {
		comment(sexp);
		int r = nextToken(sexp, '(');
		if(r == 1) {
			return readCar(sexp);
		} else if (r == 0){
			return readAtom(sexp);
		}
		return null;
	}
	
	public int nextToken(StringCharacterIterator sexp, char n) throws IOException {
		char c;
		do {
			c = sexp.next();
			if(c == CharacterIterator.DONE)
				return -1;
		} while(c == ' ');
		
		if(c == n) {
			return 1;
		} else {
			sexp.previous();
			return 0;
		}
	}
	
	public void comment(StringCharacterIterator sexp) throws IOException {
		int r = nextToken(sexp, ';');
		if(r == 1) {
			char c;
			while((c = sexp.next()) != CharacterIterator.DONE) {
				if(c == '\r' || c == '\n')
					break;
			}
			comment(sexp);
		}
	}
	
	public Cell readCdr(StringCharacterIterator sexp) throws IOException {
		comment(sexp);
		int r = nextToken(sexp, ')');
		if(r == 0) {
			return readCar(sexp);
		}
		return null;
	}
	
	public Cell readCar(StringCharacterIterator sexp) throws IOException {
		comment(sexp);
		int r = nextToken(sexp, ')');
		if (r == 0) {
			Cell cell = new Cell();
			cell.setCar(read(sexp));
			cell.setCdr(readCdr(sexp));
			return cell;
		}
		return null;
	}
	
	public Cell readAtom(StringCharacterIterator sexp) throws IOException {
		comment(sexp);
		char c = sexp.next();
		
		if(c == ' ' || c == '(' || c == ')' || c == CharacterIterator.DONE) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder().append(c);
			while(true) {
				c = sexp.next();
				sb.append(c);
				if(c == ' ' || c == '(' || c == ')' || c == CharacterIterator.DONE) {
					sexp.previous();
					sb.deleteCharAt(sb.length()-1);
					Cell cell = new Cell();
					cell.setAtom(sb.toString());
					return cell;
				}
			}
		}
	}
}
