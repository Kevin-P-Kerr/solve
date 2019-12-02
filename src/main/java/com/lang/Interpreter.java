package com.lang;

import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.google.common.collect.Lists;
import com.lang.parse.Tokenizer.Token;
import com.lang.parse.Tokenizer.TokenStream;
import com.lang.parse.Tokenizer.Token.TokenType;
import com.lang.val.AxiomSet;
import com.lang.val.Prop;

public class Interpreter {

	private final TokenStream tokens;
	private static final int NUM_CPUS = Runtime.getRuntime().availableProcessors();
	private static final ExecutorService exec = Executors.newFixedThreadPool(NUM_CPUS - 2);

	public Interpreter(TokenStream s) {
		this.tokens = s;
	}

	public void eval() throws Exception {
		List<Prop> axioms = Lists.newArrayList();
		while (tokens.hasToken()) {
			Token t = tokens.peek();
			switch (t.getType()) {
			case TT_FORALL:
			case TT_THEREIS:
				Prop ax = ParseProp(tokens);
				axioms.add(ax);
				break;
			case TT_VAR:
				if (t.getLit().equals("get")) {
					tokens.getNext(); // throw the "get" away
					int order = parseInt(tokens);
					int resources = parseInt(tokens);
					int interestingLimit = parseInt(tokens);

					AxiomSet as = new AxiomSet(axioms);

					List<Prop> pl = as.getConclusionsOfOrderN(order, resources);
					for (Prop p : pl) {
						if (!p.isInteresting(interestingLimit)) {
							continue;
						}
						System.out.println(p.toString());
					}
				} else if (t.getLit().equals("prove")) {
					tokens.getNext(); // throw the "prove" away
					int order = parseInt(tokens);
					int resources = parseInt(tokens);

					Prop p = ParseProp(tokens);
					AxiomSet as = new AxiomSet(axioms);

					System.out.println("attempting proof of " + p.toString());
					if (as.contradicts(p, order, resources)) {
						System.out.println("proven false");
					} else {
						Prop neg = p.negate();
						System.out.println("negating:  " + neg.toString());

						if (as.contradicts(neg, order, resources)) {

							System.out.println("proven true");

						} else {
							System.out.println("cannot prove true or false given resources");
						}
					}
				} else if (t.getLit().equals("betterProve")) {
					tokens.getNext(); // throw the "prove" away
					Prop p = ParseProp(tokens);
					AxiomSet as1 = new AxiomSet(axioms);
					AxiomSet as2 = new AxiomSet(axioms);

					System.out.println("attempting proof of " + p.toString());
					ProofTask pt = new ProofTask(as1, p, 100);
					ProofTask ptt = new ProofTask(as2, p.negate(), true, 100);
					CompletionService<ProofResult> cs = new ExecutorCompletionService<>(exec);
					Future<ProofResult> f1 = cs.submit(ptt);
					Future<ProofResult> f2 = cs.submit(pt);
					Future<ProofResult> r = null;
					try {
						r = cs.poll(300, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} finally {
						f1.cancel(true);
						f2.cancel(true);
						ProofResult ret;
						if (r == null) {
							ret = null;
						} else {
							ret = r.get();
						}
						switch (ret.getProofValue()) {
						case PF_PROVED_FALSE:
							System.out.println("proven false");
							break;
						case PF_PROVED_TRUE:
							System.out.println("proven true");
							break;
						case PF_UNPROVED:
							System.out.println("could not prove in given time");
							break;
						default:
							break;

						}
					}
				} else if (t.getLit().equals("negate")) {
					System.out.println("negating");
					tokens.getNext();
					Prop p = ParseProp(tokens);
					p = p.negate();
					System.out.println(p.toString());
				} else {
					System.out.println("what's this? " + t.getLit());
					System.exit(1);
				}
				break;
			default:
				System.err.println("parse error");
				System.exit(1);
			}
		}
	}

	private int parseInt(TokenStream tokens) {
		Token t = tokens.getNext();
		return Integer.parseInt(t.getLit());
	}

	private Prop ParseProp(TokenStream tokens) throws Exception {
		Prop.QuantifierPart q = parseQuantifierPart(tokens);
		Prop.BooleanPart b = parseBoolean(tokens);
		return new Prop(q, b);
	}

	private Prop.QuantifierPart parseQuantifierPart(TokenStream tokens) throws Exception {
		List<Prop.Quantifier> quantifiers = Lists.newArrayList();
		Token t = tokens.peek();
		while (t.getType() != TokenType.TT_COLON) {
			Prop.Quantifier q = parseQuantifier(tokens);
			quantifiers.add(q);
			t = tokens.peek();
		}
		// throw away the colon
		tokens.getNext();
		return new Prop.QuantifierPart(quantifiers);
	}

	private Prop.BooleanPart parseBoolean(TokenStream tokens) throws Exception {
		Token t = tokens.peek();
		List<Prop.ConjunctProp> cons = Lists.newArrayList();
		while (t.getType() != TokenType.TT_PERIOD) {
			Prop.ConjunctProp cp = parseConjunctProp(tokens);
			cons.add(cp);
			t = tokens.peek();
			if (t.getType() == TokenType.TT_PLUS) {
				t = tokens.getNext(); // throw away the plus sign
			}
		}
		tokens.getNext(); // throw away the period
		return new Prop.BooleanPart(cons);
	}

	// conjunctionProp = atomicProp || atomicProp*conjunctProp
	private Prop.ConjunctProp parseConjunctProp(TokenStream tokens) throws Exception {
		List<Prop.AtomicProp> atoms = parseAtoms(tokens);
		return new Prop.ConjunctProp(atoms);
	}

	List<Prop.AtomicProp> parseAtoms(TokenStream tokens) throws Exception {
		List<Prop.AtomicProp> atoms = Lists.newArrayList();
		atoms.add(parseAtom(tokens));
		Token t = tokens.peek();
		if (t.getType() == TokenType.TT_ASTER) {
			tokens.getNext();
			atoms.addAll(parseAtoms(tokens));
		}
		return atoms;
	}

	private Prop.AtomicProp parseAtom(TokenStream tokens) throws Exception {
		Token t = tokens.getNext();
		boolean negate = false;
		if (t.getType() == TokenType.TT_TILDE) {
			negate = true;
			t = tokens.getNext();
		}
		String name = t.getLit();
		List<Prop.Heccity> hecs = Lists.newArrayList();
		t = tokens.getNext();
		if (t.getType() != TokenType.TT_LPAREN) {
			throw new Exception();
		}
		t = tokens.getNext();
		while (t.getType() != TokenType.TT_RPAREN) {
			Prop.Heccity h = new Prop.Heccity(t.getLit());
			hecs.add(h);
			t = tokens.getNext();
		}

		return new Prop.AtomicProp(negate, name, hecs);

	}

	private Prop.Quantifier parseQuantifier(TokenStream tokens) throws Exception {
		Token t = tokens.getNext();
		switch (t.getType()) {
		case TT_FORALL:
			return Prop.Quantifier.newUniversal(tokens.getNext().getLit());
		case TT_THEREIS:
			return Prop.Quantifier.newExistential(tokens.getNext().getLit());
		}
		throw new Exception("bad quantifier");
	}

}
