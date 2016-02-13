package org.matheclipse.core.reflection.system;

import static org.matheclipse.core.expression.F.C1D2;
import static org.matheclipse.core.expression.F.C2;
import static org.matheclipse.core.expression.F.CN1;
import static org.matheclipse.core.expression.F.Cos;
import static org.matheclipse.core.expression.F.Cosh;
import static org.matheclipse.core.expression.F.Plus;
import static org.matheclipse.core.expression.F.Power;
import static org.matheclipse.core.expression.F.Sin;
import static org.matheclipse.core.expression.F.Sinh;
import static org.matheclipse.core.expression.F.Subtract;
import static org.matheclipse.core.expression.F.Times;
import static org.matheclipse.core.expression.F.x;
import static org.matheclipse.core.expression.F.x_;
import static org.matheclipse.core.expression.F.y;
import static org.matheclipse.core.expression.F.y_;

import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.exception.Validate;
import org.matheclipse.core.eval.interfaces.AbstractEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.IInteger;
import org.matheclipse.core.interfaces.ISymbol;
import org.matheclipse.core.patternmatching.HashedOrderlessMatcher;
import org.matheclipse.core.visit.VisitorExpr;

/**
 * Transform products of trigonometric functions into &quot;linear form&quot;.
 * 
 * <a href="http://en.wikipedia.org/wiki/List_of_trigonometric_identities#Product-to-sum_and_sum-to-product_identities" >List of
 * trigonometric identities - Product-to-sum and sum-to-product identities</a>
 */
public class TrigReduce extends AbstractEvaluator {
	private static HashedOrderlessMatcher ORDERLESS_MATCHER = new HashedOrderlessMatcher();

	public TrigReduce() {
	}

	static class TrigReduceVisitor extends VisitorExpr {
		public TrigReduceVisitor() {
			super();
		}

		@Override
		public IExpr visit(IAST ast) {
			if (ast.isTimes()) {
				IAST result = ORDERLESS_MATCHER.evaluate(ast);
				if (result.isPresent()) {
					return result;
				}
			} else if (ast.isPower()) {
				if (ast.arg1().isAST() && ast.arg2().isInteger()) {
					IInteger n = (IInteger) ast.arg2();
					if (n.isPositive()) {
						IAST powerArg1 = (IAST) ast.arg1();
						IExpr x;
						if (powerArg1.isSin()) {
							x = powerArg1.arg1();
							// (1/2-Cos[2*x]/2)*Sin[x]^(n-2)
							return Times(Subtract(C1D2, Times(C1D2, Cos(Times(C2, x)))), Power(Sin(x), n.subtract(C2)));
						} else if (powerArg1.isCos()) {
							x = powerArg1.arg1();
							// (1/2+Cos[2*x]/2)*Cos[x]^(n-2)
							return Times(Plus(C1D2, Times(C1D2, Cos(Times(C2, x)))), Power(Cos(x), n.subtract(C2)));
						}
					}
				}
			}
			return visitAST(ast);
		}
	}

	@Override
	public IExpr evaluate(final IAST ast, EvalEngine engine) {
		Validate.checkSize(ast, 2);

		TrigReduceVisitor trigReduceVisitor = new TrigReduceVisitor();
		IExpr temp = ast.arg1();
		IExpr result = temp;
		while (temp.isPresent()) {
			result = temp;
			if (temp.isPlus() || temp.isTimes() || temp.isPower()) {
				result = F.evalExpand(temp);
			}

			temp = result.accept(trigReduceVisitor);
			if (temp.isPresent()) {
				result = temp;
			}
		}
		return result;
	}

	@Override
	public void setUp(final ISymbol symbol) {
		// ORDERLESS_MATCHER.setUpHashRule("Sin[x_]", "Cos[y_]", "Sin[x+y]/2+Sin[x-y]/2");
		ORDERLESS_MATCHER.defineHashRule(Sin(x_), Cos(y_),
				Plus(Times(C1D2, Sin(Plus(x, y))), Times(C1D2, Sin(Plus(x, Times(CN1, y))))));
		// ORDERLESS_MATCHER.setUpHashRule("Sin[x_]", "Sin[y_]", "Cos[x-y]/2-Cos[x+y]/2");
		ORDERLESS_MATCHER.defineHashRule(Sin(x_), Sin(y_),
				Plus(Times(C1D2, Cos(Plus(x, Times(CN1, y)))), Times(CN1, C1D2, Cos(Plus(x, y)))));
		// ORDERLESS_MATCHER.setUpHashRule("Cos[x_]", "Cos[y_]", "Cos[x+y]/2+Cos[x-y]/2");
		ORDERLESS_MATCHER.defineHashRule(Cos(x_), Cos(y_),
				Plus(Times(C1D2, Cos(Plus(x, y))), Times(C1D2, Cos(Plus(x, Times(CN1, y))))));
		// ORDERLESS_MATCHER.setUpHashRule("Sinh[x_]", "Cosh[y_]", "Sinh[x-y]/2+Sinh[x+y]/2");
		ORDERLESS_MATCHER.defineHashRule(Sinh(x_), Cosh(y_),
				Plus(Times(C1D2, Sinh(Plus(x, Times(CN1, y)))), Times(C1D2, Sinh(Plus(x, y)))));
		// ORDERLESS_MATCHER.setUpHashRule("Tan[x_]", "Tan[y_]", "(Cos[x - y] - Cos[x + y])/(Cos[x - y] + Cos[x + y])");
		// ORDERLESS_MATCHER.defineHashRule(Tan(x_), Tan(y_),
		// Divide(Subtract(Cos(Subtract(x,y)),Cos(Plus(x,y))),Plus(Cos(Subtract(x,y)),Cos(Plus(x,y)))));
		symbol.setAttributes(ISymbol.LISTABLE);
	}

}
