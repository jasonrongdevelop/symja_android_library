package org.matheclipse.core.eval.interfaces;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.matheclipse.core.basic.Config;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.expression.AST2;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IASTAppendable;
import org.matheclipse.core.interfaces.IASTMutable;
import org.matheclipse.core.interfaces.IComplex;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.INumber;
import org.matheclipse.core.interfaces.ISymbol;
import org.matheclipse.core.patternmatching.PatternMatcherAndInvoker;

/**
 * Abstract interface for built-in Symja functions. The <code>numericEval()</code> method delegates to the
 * <code>evaluate()</code>
 * 
 */
public abstract class AbstractFunctionEvaluator extends AbstractEvaluator {

	/**
	 * Check if the expression has a complex number factor I.
	 * 
	 * @param expression
	 * @param factor
	 * @return the negated negative expression or <code>null</code> if a negative expression couldn't be extracted.
	 */
	public static IExpr extractFactorFromExpression(final IExpr expression, INumber factor) {
		return extractFactorFromExpression(expression, factor, true);
	}

	/**
	 * Check if the expression has a complex number factor I.
	 * 
	 * @param expression
	 * @param factor
	 * @param checkTimes
	 *            check <code>Times(...)</code> expressions
	 * @return the negated negative expression or <code>F.NIL</code> if a negative expression couldn't be extracted.
	 */
	public static IExpr extractFactorFromExpression(final IExpr expression, INumber factor, boolean checkTimes) {
		if (expression.isNumber()) {
			if (((INumber) expression).equals(factor)) {
				return F.C1;
			}
		} else {
			if (expression.isAST()) {
				if (checkTimes && expression.isTimes()) {
					IAST timesAST = ((IAST) expression);
					IExpr arg1 = timesAST.arg1();
					if (arg1.isNumber()) {
						if (((INumber) arg1).isImaginaryUnit()) {
							return timesAST.rest().getOneIdentity(factor);
						}
					}
				}
			}
		}
		return F.NIL;
	}

	/**
	 * Check if the expression is canonical negative.
	 * 
	 * @param expression
	 * @return the negated negative expression or <code>F.NIL</code> if a negative expression couldn't be extracted.
	 */
	public static IExpr getNormalizedNegativeExpression(final IExpr expression) {
		return getNormalizedNegativeExpression(expression, true);
	}

	/**
	 * Check if the expression is canonical negative.
	 * 
	 * @param expression
	 * @param checkTimesPlus
	 *            check <code>Times(...)</code> and <code>Plus(...)</code> expressions
	 * @return the negated negative expression or <code>F.NIL</code> if a negative expression couldn't be extracted.
	 */
	public static IExpr getNormalizedNegativeExpression(final IExpr expression, boolean checkTimesPlus) {
		IASTMutable result = F.NIL;
		if (expression.isNumber()) {
			if (((INumber) expression).complexSign() < 0) {
				return ((INumber) expression).negate();
			}
			return F.NIL;
		}
		if (expression.isAST()) {
			if (checkTimesPlus && expression.isTimes()) {
				IAST timesAST = ((IAST) expression);
				IExpr arg1 = timesAST.arg1();
				if (arg1.isNumber()) {
					if (((INumber) arg1).complexSign() < 0) {
						IExpr negNum = ((INumber) arg1).negate();
						if (negNum.isOne()) {
							return timesAST.rest().getOneIdentity(F.C1);
						}
						return timesAST.setAtClone(1, negNum);
					}
				} else if (arg1.isNegativeInfinity()) {
					return timesAST.setAtClone(1, F.CInfinity);
				} else if (arg1.isNegative()) {
					IExpr negNum = arg1.negate();
					return timesAST.setAtClone(1, negNum);
				}
			} else if (checkTimesPlus && expression.isPlus()) {
				IAST plusAST = ((IAST) expression);
				IExpr arg1 = plusAST.arg1();
				if (arg1.isNumber()) {
					if (((INumber) arg1).complexSign() < 0) {
						result = plusAST.copy();
						result.set(1, arg1.negate());
						for (int i = 2; i < plusAST.size(); i++) {
							result.set(i, plusAST.get(i).negate());
						}
						return result;
					}
				} else if (arg1.isNegativeInfinity()) {
					result = plusAST.copy();
					result.set(1, F.CInfinity);
					for (int i = 2; i < plusAST.size(); i++) {
						result.set(i, plusAST.get(i).negate());
					}
					return result;
				} else if (arg1.isTimes()) {
					IExpr arg1Negated = getNormalizedNegativeExpression(arg1, checkTimesPlus);
					if (arg1Negated.isPresent()) {
						// int positiveElementsCounter = 0;
						result = plusAST.copy();
						result.set(1, arg1Negated);
						for (int i = 2; i < plusAST.size(); i++) {
							IExpr temp = plusAST.get(i);
							// if (!temp.isTimes() && !temp.isPower()) {
							// return F.NIL;
							// }

							// arg1Negated = getNormalizedNegativeExpression(temp, checkTimesPlus);
							// if (arg1Negated.isPresent()) {
							// result.set(i, arg1Negated);
							// } else {

							// positiveElementsCounter++;
							// if (positiveElementsCounter * 2 > plusAST.argSize()) {
							// number of positive elements is greater
							// than number of negative elements
							// return F.NIL;
							// }
							result.set(i, temp.negate());

							// }
						}
						return result;
					}
				}
				// } else if (expression.isNegativeInfinity()) {
				// return F.CInfinity;
			} else if (expression.isDirectedInfinity() && expression.isAST1()) {
				IExpr arg1 = expression.first();
				if (arg1.isMinusOne()) {
					return F.CInfinity;
				}
				if (arg1.isNegativeImaginaryUnit()) {
					return F.DirectedInfinity(F.CI);
				}
			}
		}
		// if (expression.isNegativeResult()) {
		// return F.eval(F.Negate(expression));
		// }
		return F.NIL;
	}

	/**
	 * Try to split a periodic part from the expression: <code>expr == part.arg1() + part.arg2() * period</code>
	 * 
	 * @param expr
	 * @param period
	 * @return <code>F.NIL</code> if no periodicity was found or the rest at argument 1 and the factor of the period at
	 *         argument 2
	 */
	public static IAST getPeriodicParts(final IExpr expr, final IExpr period) {
		// IExpr[] result = new IExpr[2];
		// result[0] = F.C0;
		// result[1] = F.C1;
		AST2 result = new AST2(F.List, F.C0, F.C1);
		if (expr.equals(period)) {
			return result;
		}
		if (expr.isAST()) {
			IAST ast = (IAST) expr;
			if (ast.isTimes()) {
				for (int i = 1; i < ast.size(); i++) {
					if (ast.get(i).equals(period)) {
						result.set(2, ast.removeAtClone(i).getOneIdentity(F.C1));
						return result;
					}
				}
				return F.NIL;
			}
			if (ast.isPlus()) {
				for (int i = 1; i < ast.size(); i++) {
					IAST temp = getPeriodicParts(ast.get(i), period);
					if (temp.isPresent() && temp.arg1().isZero()) {
						result.set(1, ast.removeAtClone(i).getOneIdentity(F.C0));
						result.set(2, temp.arg2());
						return result;
					}
				}
			}
		}
		return F.NIL;
	}

	/**
	 * Check if the expression is canonical negative.
	 * 
	 * @param expression
	 * @param checkTimesPlus
	 *            check <code>Times(...)</code> and <code>Plus(...)</code> expressions
	 * @return the negated negative expression or <code>F.NIL</code> if a negative expression couldn't be extracted.
	 */
	public static IExpr getPowerNegativeExpression(final IExpr expression, boolean checkTimesPlus) {
		IASTMutable result = F.NIL;
		if (expression.isNumber()) {
			if (((INumber) expression).complexSign() < 0) {
				return ((INumber) expression).negate();
			}
			return F.NIL;
		}
		if (expression.isAST()) {
			if (checkTimesPlus && expression.isTimes()) {
				IAST timesAST = ((IAST) expression);
				IExpr arg1 = timesAST.arg1();
				if (arg1.isNumber()) {
					if (((INumber) arg1).complexSign() < 0) {
						IExpr negNum = ((INumber) arg1).negate();
						if (negNum.isOne()) {
							return timesAST.rest().getOneIdentity(F.C1);
						}
						return timesAST.setAtClone(1, negNum);
					}
				} else if (arg1.isNegativeInfinity()) {
					return timesAST.setAtClone(1, F.CInfinity);
				} else if (arg1.isNegative()) {
					IExpr negNum = arg1.negate();
					return timesAST.setAtClone(1, negNum);
				}
			} else if (checkTimesPlus && expression.isPlus()) {
				IAST plusAST = ((IAST) expression);
				IExpr arg1 = plusAST.arg1();
				if (arg1.isNumber()) {
					if (((INumber) arg1).complexSign() < 0) {
						result = plusAST.copy();
						result.set(1, arg1.negate());
						for (int i = 2; i < plusAST.size(); i++) {
							result.set(i, plusAST.get(i).negate());
						}
						return result;
					}
				} else if (arg1.isNegativeInfinity()) {
					result = plusAST.copy();
					result.set(1, F.CInfinity);
					for (int i = 2; i < plusAST.size(); i++) {
						result.set(i, plusAST.get(i).negate());
					}
					return result;
				} else if (arg1.isTimes()) {
					IExpr arg1Negated = getPowerNegativeExpression(arg1, checkTimesPlus);
					if (arg1Negated.isPresent()) {
						int positiveElementsCounter = 0;
						result = plusAST.copy();
						result.set(1, arg1Negated);
						for (int i = 2; i < plusAST.size(); i++) {
							IExpr temp = plusAST.get(i);
							if (!temp.isTimes() && !temp.isPower()) {
								return F.NIL;
							}
							arg1Negated = getPowerNegativeExpression(temp, checkTimesPlus);
							if (arg1Negated.isPresent()) {
								result.set(i, arg1Negated);
							} else {
								positiveElementsCounter++;
								if (positiveElementsCounter * 2 > plusAST.argSize()) {
									// number of positive elements is greater
									// than number of negative elements
									return F.NIL;
								}
								result.set(i, temp.negate());
							}
						}
						return result;
					}
				}
				// } else if (expression.isNegativeInfinity()) {
				// return F.CInfinity;
			} else if (expression.isDirectedInfinity() && expression.isAST1()) {
				IExpr arg1 = expression.first();
				if (arg1.isMinusOne()) {
					return F.CInfinity;
				}
				if (arg1.isNegativeImaginaryUnit()) {
					return F.DirectedInfinity(F.CI);
				}
			}
		}
		// if (expression.isNegativeResult()) {
		// return F.eval(F.Negate(expression));
		// }
		return F.NIL;
	}

	/**
	 * Check if <code>expr</code> is a pure imaginary number without a real part.
	 * 
	 * @param expr
	 * @return <code>F.NIL</code>, if <code>expr</code> is not a pure imaginary number.
	 */
	public static IExpr getPureImaginaryPart(final IExpr expr) {
		IExpr temp = pureImaginaryPart(expr);
		if (temp.isPresent()) {
			return temp;
		}
		if (expr.isPlus()) {
			IAST plus = ((IAST) expr);
			IExpr arg = pureImaginaryPart(plus.arg1());
			if (arg.isPresent()) {
				IASTAppendable result = plus.setAtClone(1, arg);
				for (int i = 2; i < plus.size(); i++) {
					arg = pureImaginaryPart(plus.get(i));
					if (!arg.isPresent()) {
						return F.NIL;
					}
					result.set(i, arg);
				}
				return result;
			}
		}
		return F.NIL;
	}

	public static IExpr imaginaryPart(final IExpr expr, boolean unequalsZero) {
		IExpr imPart = F.Im.of(expr);
		if (unequalsZero) {
			if (imPart.isZero()) {
				return F.NIL;
			}
		}
		if (imPart.isNumber() || imPart.isFree(F.Im)) {
			return imPart;
		}
		return F.NIL;
	}

	public static IExpr realPart(final IExpr expr, boolean unequalsZero) {
		IExpr rePart = F.Re.of(expr);
		if (unequalsZero) {
			if (rePart.isZero()) {
				return F.NIL;
			}
		}
		if (rePart.isNumber() || rePart.isFree(F.Re)) {
			return rePart;
		}
		return F.NIL;
	}
	
	/**
	 * Initialize the serialized Rubi integration rules from ressource <code>/ser/integrate.ser</code>.
	 * 
	 * @param symbol
	 */
	public static void initSerializedRules(final ISymbol symbol) {
		EvalEngine engine = EvalEngine.get();
		boolean oldPackageMode = engine.isPackageMode();
		boolean oldTraceMode = engine.isTraceMode();
		try {
			engine.setPackageMode(true);
			engine.setTraceMode(false);

			InputStream in = AbstractFunctionEvaluator.class
					.getResourceAsStream("/ser/" + symbol.getSymbolName().toLowerCase(Locale.ENGLISH) + ".ser");
			ObjectInputStream ois = new ObjectInputStream(in);
			// InputStream in = new FileInputStream("c:\\temp\\ser\\" +
			// symbol.getSymbolName() + ".ser");
			// read files with BufferedInputStream to improve performance
			// ObjectInputStream ois = new ObjectInputStream(new
			// BufferedInputStream(in));
			symbol.readRules(ois);
			ois.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			engine.setPackageMode(oldPackageMode);
			engine.setTraceMode(oldTraceMode);
		}
	}

	private static IExpr pureImaginaryPart(final IExpr expr) {
		if (expr.isComplex() && ((IComplex) expr).re().isZero()) {
			IComplex compl = (IComplex) expr;
			return compl.im();
		}
		if (expr.isTimes()) {
			IAST times = ((IAST) expr);
			IExpr arg1 = times.arg1();
			if (arg1.isComplex() && ((IComplex) arg1).re().isZero()) {
				return times.setAtClone(1, ((IComplex) arg1).im());
			}
		}
		return F.NIL;
	}

	/**
	 * Create a rule which invokes the method name in this class instance.
	 * 
	 * @param symbol
	 * @param patternString
	 * @param methodName
	 */
	public void createRuleFromMethod(ISymbol symbol, String patternString, String methodName) {
		PatternMatcherAndInvoker pm = new PatternMatcherAndInvoker(patternString, this, methodName);
		symbol.putDownRule(pm);
	}

	/** {@inheritDoc} */
	@Override
	abstract public IExpr evaluate(final IAST ast, @Nonnull EvalEngine engine);

	/**
	 * Get the predefined rules for this function symbol.
	 * 
	 * @return <code>null</code> if no rules are defined
	 * 
	 */
	public IAST getRuleAST() {
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public void setUp(final ISymbol newSymbol) {

		if (getRuleAST() != null) {
			// don't call EvalEngine#addRules() here!
			// the rules should add themselves
			// EvalEngine.get().addRules(ruleList);
		}

		// F.SYMBOL_OBSERVER.createPredefinedSymbol(newSymbol.toString());
		if (Config.SERIALIZE_SYMBOLS && newSymbol.containsRules()) {
			FileOutputStream out;
			try {
				out = new FileOutputStream("c:\\temp\\ser\\" + newSymbol.getSymbolName() + ".ser");
				ObjectOutputStream oos = new ObjectOutputStream(out);
				newSymbol.writeRules(oos);
				oos.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}