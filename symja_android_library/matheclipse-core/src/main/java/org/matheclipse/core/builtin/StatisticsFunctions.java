package org.matheclipse.core.builtin;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apfloat.ApcomplexMath;
import org.apfloat.ApfloatMath;
import org.hipparchus.distribution.RealDistribution;
import org.hipparchus.linear.FieldMatrix;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.random.RandomDataGenerator;
import org.hipparchus.stat.StatUtils;
import org.matheclipse.core.basic.Config;
import org.matheclipse.core.convert.Convert;
import org.matheclipse.core.eval.EvalAttributes;
import org.matheclipse.core.eval.EvalEngine;
import org.matheclipse.core.eval.exception.Validate;
import org.matheclipse.core.eval.exception.WrongArgumentType;
import org.matheclipse.core.eval.interfaces.AbstractArg2;
import org.matheclipse.core.eval.interfaces.AbstractEvaluator;
import org.matheclipse.core.eval.interfaces.AbstractFunctionEvaluator;
import org.matheclipse.core.eval.interfaces.AbstractMatrix1Expr;
import org.matheclipse.core.eval.interfaces.AbstractTrigArg1;
import org.matheclipse.core.expression.ASTRealMatrix;
import org.matheclipse.core.expression.ASTRealVector;
import org.matheclipse.core.expression.ApcomplexNum;
import org.matheclipse.core.expression.ApfloatNum;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.IAST;
import org.matheclipse.core.interfaces.IASTAppendable;
import org.matheclipse.core.interfaces.IBuiltInSymbol;
import org.matheclipse.core.interfaces.IComplexNum;
import org.matheclipse.core.interfaces.IDiscreteDistribution;
import org.matheclipse.core.interfaces.IDistribution;
import org.matheclipse.core.interfaces.IEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.matheclipse.core.interfaces.IFraction;
import org.matheclipse.core.interfaces.IInteger;
import org.matheclipse.core.interfaces.INum;
import org.matheclipse.core.interfaces.IRational;
import org.matheclipse.core.interfaces.ISignedNumber;
import org.matheclipse.core.interfaces.ISymbol;
import org.matheclipse.core.reflection.system.rules.QuantileRules;
import org.matheclipse.core.reflection.system.rules.StandardDeviationRules;
import org.uncommons.maths.random.BinomialGenerator;
import org.uncommons.maths.random.DiscreteUniformGenerator;
import org.uncommons.maths.random.ExponentialGenerator;
import org.uncommons.maths.random.GaussianGenerator;
import org.uncommons.maths.random.PoissonGenerator;

public class StatisticsFunctions {
	private static final double NEXTDOWNONE = Math.nextDown(1.0);

	static {
		F.ArithmeticGeometricMean.setEvaluator(new ArithmeticGeometricMean());
		F.CDF.setEvaluator(new CDF());
		F.PDF.setEvaluator(new PDF());
		F.BernoulliDistribution.setEvaluator(new BernoulliDistribution());
		F.BinCounts.setEvaluator(new BinCounts());
		F.BinomialDistribution.setEvaluator(new BinomialDistribution());
		F.CentralMoment.setEvaluator(new CentralMoment());
		F.ChiSquareDistribution.setEvaluator(new ChiSquareDistribution());
		F.Correlation.setEvaluator(new Correlation());
		F.Covariance.setEvaluator(new Covariance());
		F.DiscreteUniformDistribution.setEvaluator(new DiscreteUniformDistribution());
		F.ErlangDistribution.setEvaluator(new ErlangDistribution());
		F.Expectation.setEvaluator(new Expectation());
		F.ExponentialDistribution.setEvaluator(new ExponentialDistribution());
		F.FiveNum.setEvaluator(new FiveNum());
		F.FRatioDistribution.setEvaluator(new FRatioDistribution());
		F.FrechetDistribution.setEvaluator(new FrechetDistribution());
		F.GammaDistribution.setEvaluator(new GammaDistribution());
		F.GeometricMean.setEvaluator(new GeometricMean());
		F.GeometricDistribution.setEvaluator(new GeometricDistribution());
		F.GumbelDistribution.setEvaluator(new GumbelDistribution());
		F.HypergeometricDistribution.setEvaluator(new HypergeometricDistribution());
		// F.KolmogorovSmirnovTest.setEvaluator(new KolmogorovSmirnovTest());
		F.Kurtosis.setEvaluator(new Kurtosis());
		F.LogNormalDistribution.setEvaluator(new LogNormalDistribution());
		F.Mean.setEvaluator(new Mean());
		F.MeanDeviation.setEvaluator(new MeanDeviation());
		F.Median.setEvaluator(new Median());
		F.NakagamiDistribution.setEvaluator(new NakagamiDistribution());
		F.NormalDistribution.setEvaluator(new NormalDistribution());
		F.PoissonDistribution.setEvaluator(new PoissonDistribution());
		F.Probability.setEvaluator(new Probability());
		F.Quantile.setEvaluator(new Quantile());
		F.RandomVariate.setEvaluator(new RandomVariate());
		F.Rescale.setEvaluator(new Rescale());
		F.Skewness.setEvaluator(new Skewness());
		F.StandardDeviation.setEvaluator(new StandardDeviation());
		F.Standardize.setEvaluator(new Standardize());
		F.StudentTDistribution.setEvaluator(new StudentTDistribution());
		F.UniformDistribution.setEvaluator(new UniformDistribution());
		F.Variance.setEvaluator(new Variance());
		F.WeibullDistribution.setEvaluator(new WeibullDistribution());
	}

	private static IDistribution getDistribution(final IExpr arg1) {
		return (IDistribution) ((IBuiltInSymbol) arg1.head()).getEvaluator();
	}

	private static IDiscreteDistribution getDiscreteDistribution(final IExpr arg1) {
		return (IDiscreteDistribution) ((IBuiltInSymbol) arg1.head()).getEvaluator();
	}

	/**
	 * Capability to produce random variate.
	 */
	interface IRandomVariate {
		/**
		 * @param distribution
		 *            the distribution
		 * @return sample generated using the given random generator
		 */
		IExpr randomVariate(Random random, IAST distribution);
	}

	/**
	 * Functionality for a discrete probability distribution
	 */
	interface IExpectationDiscreteDistribution extends IDiscreteDistribution {
		/** @return lowest value a random variable from this distribution may attain */
		IExpr lowerBound(IAST dist);

		IExpr randomVariate(Random random, IAST dist);

		/**
		 * @param n
		 * @return P(X == n), i.e. probability of random variable X == n
		 */
		IExpr p_equals(IAST dist, IExpr n);
	}

	/**
	 * Any distribution for which an analytic expression of the variance exists should implement {@link IVariance}.
	 * 
	 * <p>
	 * The function is used in {@link Expectation} to provide the variance of a given {@link IDistribution}.
	 */
	public interface IVariance {
		/** @return variance of distribution */
		IExpr variance(IAST distribution);
	}

	/**
	 * Cumulative distribution function
	 * 
	 * ICDF extends the capabilities of {@link IPDF}
	 * 
	 * 
	 */
	public interface ICDF {
		static final IExpr CDF_NUMERIC_THRESHOLD = F.num(1e-14);

		public IExpr cdf(IAST dist, IExpr x);

	}

	/**
	 * probability density function
	 * 
	 */
	interface IPDF {
		/**
		 * 
		 * <p>
		 * For {@link IExpectationDiscreteDistribution}, the function returns the P(X == x), i.e. probability of random
		 * variable X == x
		 * 
		 * <p>
		 * For continuous distributions, the function
		 * <ul>
		 * <li>returns the value of the probability density function, which is <em>not</em> identical to P(X == x)]
		 * </ul>
		 * 
		 * @param x
		 * @return
		 */
		IExpr pdf(IAST dist, IExpr x);

		/**
		 * Call the pure PDF function.
		 * 
		 * @param pureFunction
		 * @param x
		 *            if <code>F.NIL</code> return the pure function unevaluated. If <code>List(...)</code> map the pure
		 *            function over all elements.
		 * @return
		 */
		default IExpr callFunction(IExpr pureFunction, IExpr x) {
			if (x.isPresent()) {
				if (x.isList()) {
					return ((IAST) x).map(v -> F.unaryAST1(pureFunction, v), 1);
				}
				return F.unaryAST1(pureFunction, x);
			}
			return pureFunction;
		}
	}

	/**
	 * <pre>
	 * ArithmeticGeometricMean(a, b)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the arithmetic geometric mean of <code>a</code> and <code>b</code>.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Arithmetic%E2%80%93geometric_mean">Wikipedia - Arithmetic-geometric
	 * mean)</a></li>
	 * </ul>
	 */
	private static class ArithmeticGeometricMean extends AbstractArg2 {

		@Override
		public IExpr e2ApcomplexArg(final ApcomplexNum a, final ApcomplexNum b) {
			return F.complexNum(ApcomplexMath.agm(a.apcomplexValue(), b.apcomplexValue()));
		}

		@Override
		public IExpr e2ApfloatArg(final ApfloatNum a, final ApfloatNum b) {
			return F.num(ApfloatMath.agm(a.apfloatValue(), b.apfloatValue()));
		}

		@Override
		public IExpr e2DblComArg(final IComplexNum a, final IComplexNum b) {
			IComplexNum a1 = a;
			IComplexNum b1 = b;
			while (a1.subtract(b1).abs().evalDouble() >= Config.DOUBLE_TOLERANCE) {
				IComplexNum arith = a1.add(b1).multiply(F.complexNum(1 / 2.0));
				IComplexNum geom = a1.multiply(b1).pow(F.complexNum(1 / 2.0));
				a1 = arith;
				b1 = geom;
			}
			return a1;
		}

		@Override
		public IExpr e2DblArg(final INum a, final INum b) {
			double a1 = a.doubleValue();
			double b1 = b.doubleValue();
			while (Math.abs(a1 - b1) >= Config.DOUBLE_TOLERANCE) {
				double arith = (a1 + b1) / 2.0;
				double geom = Math.sqrt(a1 * b1);
				a1 = arith;
				b1 = geom;
			}
			return F.num(a1);
		}

		public IExpr e2ObjArg(final IExpr a, final IExpr b) {
			if (a.isZero() || a.equals(b)) {
				return a;
			}
			if (b.isZero()) {
				return b;
			}
			if (a.isOne() || b.isInfinity()) {
				return F.CInfinity;
			}
			IExpr div = F.Divide.of(b, a);
			if (div.equals(F.CSqrt2)) {
				return F.Times(F.CSqrt2, F.a, F.Power(F.Pi, F.CN1D2), F.Sqr(F.Gamma(F.QQ(3L, 4L))));
			}
			return F.NIL;
		}

		/** {@inheritDoc} */
		@Override
		public void setUp(final ISymbol newSymbol) {
			newSymbol.setAttributes(ISymbol.LISTABLE | ISymbol.ORDERLESS | ISymbol.NUMERICFUNCTION);
			super.setUp(newSymbol);
		}
	}

	/**
	 * <pre>
	 * CDF(distribution, value)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the cumulative distribution function of <code>value</code>.
	 * </p>
	 * </blockquote>
	 * 
	 * <pre>
	 * PDF(distribution, {list} )
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the cumulative distribution function of the values of list.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Cumulative_distribution_function">Wikipedia - cumulative distribution
	 * function</a></li>
	 * </ul>
	 * <p>
	 * <code>CDF</code> can be applied to the following distributions:
	 * </p>
	 * <blockquote>
	 * <p>
	 * <a href="BernoulliDistribution.md">BernoulliDistribution</a>,
	 * <a href="BinomialDistribution.md">BinomialDistribution</a>,
	 * <a href="DiscreteUniformDistribution.md">DiscreteUniformDistribution</a>,
	 * <a href="ErlangDistribution.md">ErlangDistribution</a>,
	 * <a href="ExponentialDistribution.md">ExponentialDistribution</a>,
	 * <a href="FrechetDistribution.md">FrechetDistribution</a>, <a href="GammaDistribution.md">GammaDistribution</a>,
	 * <a href="GeometricDistribution.md">GeometricDistribution</a>,
	 * <a href="GumbelDistribution.md">GumbelDistribution</a>,
	 * <a href="HypergeometricDistribution.md">HypergeometricDistribution</a>,
	 * <a href="LogNormalDistribution.md">LogNormalDistribution</a>,
	 * <a href="NakagamiDistribution.md">NakagamiDistribution</a>,
	 * <a href="NormalDistribution.md">NormalDistribution</a>, <a href="PoissonDistribution.md">PoissonDistribution</a>,
	 * <a href="StudentTDistribution.md">StudentTDistribution</a>,
	 * <a href="WeibullDistribution.md">WeibullDistribution</a>
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; CDF(NormalDistribution(),-0.41)
	 * 0.3409
	 * 
	 * &gt;&gt; Table(CDF(NormalDistribution(0, s), x), {s, {.75, 1, 2}}, {x, -6,6}) // N
	 * {{0.0,0.0,0.0,0.00003,0.00383,0.09121,0.5,0.90879,0.99617,0.99997,1.0,1.0,1.0},{0.0,0.0,0.00003,0.00135,0.02275,0.15866,0.5,0.84134,0.97725,0.99865,0.99997,1.0,1.0},{0.00135,0.00621,0.02275,0.06681,0.15866,0.30854,0.5,0.69146,0.84134,0.93319,0.97725,0.99379,0.99865}}
	 * </pre>
	 */
	private static class CDF extends AbstractFunctionEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			// 1 or 2 arguments
			if (ast.size() == 2 || ast.size() == 3) {
				try {
					if (ast.arg1().isAST()) {
						IAST dist = (IAST) ast.arg1();
						IExpr xArg = F.NIL;
						if (ast.isAST2()) {
							xArg = ast.arg2();
						}
						if (dist.head().isSymbol()) {
							ISymbol head = (ISymbol) dist.head();

							if (dist.head().isSymbol()) {
								if (head instanceof IBuiltInSymbol) {
									IEvaluator evaluator = ((IBuiltInSymbol) head).getEvaluator();
									if (evaluator instanceof ICDF) {
										ICDF cdf = (ICDF) evaluator;
										return cdf.cdf(dist, xArg);
									}
								}
							}
						}
					}
				} catch (Exception ex) {
					if (Config.SHOW_STACKTRACE) {
						ex.printStackTrace();
					}
				}
			}

			return F.NIL;
		}

	}

	/**
	 * <pre>
	 * BernoulliDistribution(p)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the Bernoulli distribution.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:<br />
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Bernoulli_distribution">Wikipedia - Bernoulli distribution</a></li>
	 * </ul>
	 * <h3>Examples</h3>
	 * <p>
	 * The probability density function of the Bernoulli distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; PDF(BernoulliDistribution(p), x)
	 * Piecewise({{1-p,x==0},{p,x==1}},0)
	 * </pre>
	 * <p>
	 * The cumulative distribution function of the Bernoulli distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; CDF(BernoulliDistribution(p), x)
	 * Piecewise({{0,x&lt;0},{1-p,0&lt;=x&amp;&amp;x&lt;1}},1)
	 * </pre>
	 * <p>
	 * The mean of the Bernoulli distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; Mean(BernoulliDistribution(p))
	 * p
	 * </pre>
	 * <p>
	 * The standard deviation of the Bernoulli distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; StandardDeviation(BernoulliDistribution(p))
	 * Sqrt((1-p)*p)
	 * </pre>
	 * <p>
	 * The variance of the Bernoulli distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; Variance(BernoulliDistribution(p))
	 * (1-p)*p
	 * </pre>
	 * <p>
	 * The random variates of a Bernoulli distribution can be generated with function <code>RandomVariate</code>
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; RandomVariate(BernoulliDistribution(0.25), 10^1)
	 * {1,0,0,0,1,1,0,0,0,0}
	 * </pre>
	 * 
	 * <h3>Related terms</h3>
	 * <p>
	 * <a href="CDF.md">CDF</a>, <a href="Mean.md">Mean</a>, <a href="Mean.md">Median</a>, <a href="PDF.md">PDF</a>,
	 * <a href="Quantile.md">Quantile</a>, <a href="StandardDeviation.md">StandardDeviation</a>,
	 * <a href="Variance.md">Variance</a>
	 * </p>
	 */
	private final static class BernoulliDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance, IRandomVariate {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST1()) {
				return dist.arg1();
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST1()) {
				// (p) => Piecewise({{1, p > 1/2}}, 0)
				return F.Piecewise(F.List(F.List(F.C1, F.Greater(dist.arg1(), F.C1D2))), F.C0);
			}
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr p = dist.arg1();
				IExpr function =
						// [$ (Piecewise({{0, # < 0}, {1 - p, 0<=#<1 }}, 1)) & $]
						F.Function(
								F.Piecewise(
										F.List(F.List(F.C0, F.Less(F.Slot1, F.C0)),
												F.List(F.Plus(F.C1, F.Negate(p)),
														F.And(F.LessEqual(F.C0, F.Slot1), F.Less(F.Slot1, F.C1)))),
										F.C1)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr p = dist.arg1();
				//
				IExpr function =
						// [$ Piecewise({{1 - p, # == 0}, {p, # == 1}}, 0) & $]
						F.Function(F.Piecewise(F.List(F.List(F.Plus(F.C1, F.Negate(p)), F.Equal(F.Slot1, F.C0)),
								F.List(p, F.Equal(F.Slot1, F.C1))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST1()) {
				IExpr N = dist.arg1();
				return F.Times(N, F.Subtract(F.C1, N));
			}
			return F.NIL;
		}

		@Override
		public IExpr randomVariate(Random random, IAST dist) {
			if (dist.isAST1()) {
				double p = dist.arg1().evalDouble();
				if (0 <= p && p <= 1) {
					return F.ZZ(new BinomialGenerator(1, p, random).nextValue());
				}
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	/**
	 * <pre>
	 * BinCounts(list, width - of - bin)
	 * </pre>
	 * <p>
	 * or
	 * </p>
	 * 
	 * <pre>
	 * BinCounts(list, {min, max, width-of-bin} )
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * count the number of elements, if <code>list</code>, is divided into successive bins with width
	 * <code>width-of-bin</code>.
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; BinCounts({1,2,3,4,5},5) 
	 * {4,1}
	 * 
	 * &gt;&gt; BinCounts({1,2,3,4,5},10) 
	 * {5}
	 * </pre>
	 */
	private final static class BinCounts extends AbstractFunctionEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkRange(ast, 2, 3);

			try {
				if (ast.arg1().isList()) {
					IAST vector = (IAST) ast.arg1();
					vector = dropNonReals(engine, vector);
					if (ast.size() == 3) {
						return binCounts(vector, ast.arg2(), engine);
					} else if (ast.size() == 2) {
						return binCounts(vector, F.C1, engine);
					}

				}
			} catch (ArithmeticException rex) {
				if (Config.SHOW_STACKTRACE) {
					rex.printStackTrace();
				}
			}
			return F.NIL;
		}

		private static IExpr binCounts(IAST vector, final IExpr arg2, EvalEngine engine) {
			INum dxNum = F.CD1;
			int dx = 1;
			int xMin = 0;
			int xMax = Integer.MIN_VALUE;
			if (arg2.isList()) {
				IAST list = (IAST) arg2;
				if (list.size() == 4) {
					dx = list.arg3().toIntDefault(Integer.MIN_VALUE);
					if (dx < 0) {
						return F.NIL;
					}
					xMin = list.arg1().toIntDefault(Integer.MIN_VALUE);
					if (xMin < 0) {
						return F.NIL;
					}
					xMax = list.arg2().toIntDefault(Integer.MIN_VALUE);
					if (xMax < 0) {
						return F.NIL;
					}
					if (xMax <= xMin) {
						return F.CEmptyList;
					}
					xMin = xMin / dx;
					xMax = xMax / dx;
				}
			} else {
				dx = Integer.MIN_VALUE;
				dxNum = F.num(arg2.evalDouble());
				IExpr dXMax = F.Max.of(engine, vector);
				xMax = F.Floor.of(engine, F.Divide(F.Plus(dXMax, arg2), arg2)).toIntDefault(Integer.MIN_VALUE);
				if (xMax < 0) {
					return F.NIL;
				}
			}
			if (xMin >= 0 && xMax >= xMin) {
				int[] res = new int[xMax - xMin];
				for (int i = 1; i < vector.size(); i++) {
					IExpr temp = vector.get(i);
					int index = -1;
					if (dx != Integer.MIN_VALUE) {
						index = (((ISignedNumber) temp).floorFraction()).div(dx).toIntDefault(Integer.MIN_VALUE);
					} else {
						index = F.Floor.of(engine, (((ISignedNumber) temp).divide(dxNum)))
								.toIntDefault(Integer.MIN_VALUE);
					}
					if (index < 0 || index >= res.length) {
						engine.printMessage("BinCounts: determined not allowed bin index for " + temp.toString());
						return F.NIL;
					}
					res[index - xMin]++;
				}
				IASTAppendable result = F.ListAlloc(xMax - xMin + 1);
				for (int i = 0; i < res.length; i++) {
					result.append(F.ZZ(res[i]));
				}
				return result;
			}

			return F.NIL;
		}

		/**
		 * Drop non real expressions from this vecrtor
		 * 
		 * @param engine
		 * @param vector
		 * @return
		 */
		private static IAST dropNonReals(EvalEngine engine, IAST vector) {
			IAST[] filter = vector.filter((Function<IExpr, IExpr>) x -> {
				if (x.isReal()) {
					return x;
				}
				IExpr d = engine.evalN(x);
				if (d.isReal()) {
					return d;
				}
				return F.NIL;
			});
			vector = filter[0];
			return vector;
		}

	}

	/**
	 * <pre>
	 * BinomialDistribution(n, p)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the binomial distribution.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:<br />
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Binomial_distribution">Wikipedia - Binomial distribution</a></li>
	 * </ul>
	 * <h3>Examples</h3>
	 * <p>
	 * The probability density function of the binomial distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; PDF(BinomialDistribution(n, p), x)
	 * Piecewise({{(1-p)^(n-x)*p^x*Binomial(n,x),0&lt;=x&lt;=n}},0)
	 * </pre>
	 * <p>
	 * The cumulative distribution function of the binomial distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; CDF(BinomialDistribution(n, p), x)
	 * Piecewise({{BetaRegularized(1-p,n-Floor(x),1+Floor(x)),0&lt;=x&amp;&amp;x&lt;n},{1,x&gt;=n}},0)
	 * </pre>
	 * <p>
	 * The mean of the binomial distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; Mean(BinomialDistribution(n, p))
	 * n*p
	 * </pre>
	 * <p>
	 * The standard deviation of the binomial distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; StandardDeviation(BinomialDistribution(n, p))
	 * Sqrt(n*(1-p)*p)
	 * </pre>
	 * <p>
	 * The variance of the binomial distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; Variance(BinomialDistribution(n, p))
	 * n*(1-p)*p
	 * </pre>
	 * <p>
	 * The random variates of a binomial distribution can be generated with function <code>RandomVariate</code>
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; RandomVariate(BinomialDistribution(10,0.25), 10^1)
	 * {1,2,1,1,4,1,1,3,2,5}
	 * </pre>
	 * 
	 * <h3>Related terms</h3>
	 * <p>
	 * <a href="CDF.md">CDF</a>, <a href="Mean.md">Mean</a>, <a href="Mean.md">Median</a>, <a href="PDF.md">PDF</a>,
	 * <a href="Quantile.md">Quantile</a>, <a href="StandardDeviation.md">StandardDeviation</a>,
	 * <a href="Variance.md">Variance</a>
	 * </p>
	 */
	private final static class BinomialDistribution extends AbstractEvaluator
			implements ICDF, IDiscreteDistribution, IPDF, IVariance, IRandomVariate {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST2()) {
				return F.Times(dist.arg1(), dist.arg2());
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				IExpr function =
						// [$ (Piecewise({{BetaRegularized(1 - m, n - Floor(#), 1 + Floor(#)), 0<=#<n}, {1, # >= n}},
						// 0)) & $]
						F.Function(F.Piecewise(F.List(
								F.List(F.BetaRegularized(F.Plus(F.C1, F.Negate(m)),
										F.Plus(n, F.Negate(F.Floor(F.Slot1))), F.Plus(F.C1, F.Floor(F.Slot1))),
										F.And(F.LessEqual(F.C0, F.Slot1), F.Less(F.Slot1, n))),
								F.List(F.C1, F.GreaterEqual(F.Slot1, n))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				//
				IExpr function =
						// [$ Piecewise({{(1 - m)^(-# + n)*m^#*Binomial(n, #), 0 <= # <= n}}, 0) & $]
						F.Function(
								F.Piecewise(F.List(F.List(
										F.Times(F.Power(F.Plus(F.C1, F.Negate(m)), F.Plus(F.Negate(F.Slot1), n)),
												F.Power(m, F.Slot1), F.Binomial(n, F.Slot1)),
										F.LessEqual(F.C0, F.Slot1, n))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST2()) {
				// (1 - m) m n
				return F.Times(dist.arg1(), dist.arg2(), F.Subtract(F.C1, dist.arg2()));
			}
			return F.NIL;
		}

		@Override
		public IExpr randomVariate(Random random, IAST dist) {
			if (dist.isAST2()) {
				int n = dist.arg1().toIntDefault(-1);
				if (n > 0) {
					double p = dist.arg2().evalDouble();
					if (0 <= p && p <= 1) {
						return F.ZZ(new BinomialGenerator(n, p, random).nextValue());
					}
				}
			}
			return F.NIL;
		}
	}

	/**
	 * <pre>
	 * CentralMoment(list, r)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * gives the the <code>r</code>th central moment (i.e. the <code>r</code>th moment about the mean) of
	 * <code>list</code>.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:<br />
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Central_moment">Wikipedia - Central moment</a></li>
	 * </ul>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt;&gt; CentralMoment({1.1, 1.2, 1.4, 2.1, 2.4}, 4)
	 * 0.10085
	 * </pre>
	 */
	private final static class CentralMoment extends AbstractEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 3);
			if (ast.arg1().isList()) {
				IAST list = (IAST) ast.arg1();
				IExpr r = ast.arg2();
				return F.Divide(F.Total(F.Power(F.Subtract(list, F.Mean(list)), r)), F.Length(list));
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	private final static class ChiSquareDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			// 1 or 3 args
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST1()) {
				return dist.arg1();
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST1()) {
				IExpr v = dist.arg1();
				// [$ 2*InverseGammaRegularized(v/2, 0, 1/2) $]
				F.Times(F.C2, F.InverseGammaRegularized(F.Times(F.C1D2, v), F.C0, F.C1D2)); // $$;
			}
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr v = dist.arg1();
				IExpr function =
						// [$ Piecewise({{GammaRegularized(v/2, 0, #/2), # > 0}}, 0) & $]
						F.Function(F.Piecewise(
								F.List(F.List(F.GammaRegularized(F.Times(F.C1D2, v), F.C0, F.Times(F.C1D2, F.Slot1)),
										F.Greater(F.Slot1, F.C0))),
								F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr v = dist.arg1();
				IExpr function =
						// [$ Piecewise({{#^(-1 + v/2)/(2^(v/2)*E^(#/2)*Gamma(v/2)), # > 0}}, 0) & $]
						F.Function(
								F.Piecewise(F.List(F.List(
										F.Times(F.Power(
												F.Times(F.Power(F.C2, F.Times(F.C1D2, v)),
														F.Exp(F.Times(F.C1D2, F.Slot1)), F.Gamma(F.Times(F.C1D2, v))),
												-1), F.Power(F.Slot1, F.Plus(F.CN1, F.Times(F.C1D2, v)))),
										F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST1()) {
				IExpr v = dist.arg1();
				// 2*v
				return F.Times(F.C2, v);
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	/**
	 * <pre>
	 * Correlation(a, b)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * computes Pearson's correlation of two equal-sized vectors <code>a</code> and <code>b</code>.
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Correlation({10, 8, 13, 9, 11, 14, 6, 4, 12, 7, 5}, {8.04, 6.95, 7.58, 8.81, 8.33, 9.96, 7.24, 4.26, 10.84, 4.82, 5.68})
	 * 0.81642
	 * </pre>
	 */
	private final static class Correlation extends AbstractFunctionEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 3);
			IExpr a = ast.arg1();
			IExpr b = ast.arg2();
			int dim1 = a.isVector();
			int dim2 = b.isVector();
			if (dim1 >= 0 && dim1 == dim2) {
				return F.Divide(F.Covariance(a, b), F.Times(F.StandardDeviation(a), F.StandardDeviation(b)));
			}
			return F.NIL;
		}

	}

	/**
	 * <pre>
	 * FiveNum({dataset})
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * the Tuckey five-number summary is a set of descriptive statistics that provide information about a
	 * <code>dataset</code>. It consists of the five most important sample percentiles:
	 * </p>
	 * <ol>
	 * <li>the sample minimum (smallest observation)</li>
	 * <li>the lower quartile or first quartile</li>
	 * <li>the median (the middle value)</li>
	 * <li>the upper quartile or third quartile</li>
	 * <li>the sample maximum (largest observation)</li>
	 * </ol>
	 * </blockquote>
	 * <p>
	 * See:
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Five-number_summary">Wikipedia - Five-number summary</a></li>
	 * </ul>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; FiveNum({0, 0, 1, 2, 63, 61, 27, 13}) 
	 * {0,1/2,15/2,44,63}
	 * </pre>
	 */
	private final static class FiveNum extends AbstractEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			int size = ast.arg1().isVector();
			if (size >= 0) {
				IAST param = F.List(F.List(F.C1D2, F.C0), //
						F.List(F.C0, F.C1));

				IAST list = (IAST) ast.arg1();
				IASTAppendable result = F.ListAlloc(5);

				result.append(F.Min(list));
				result.append(F.Quantile(list, F.C1D4, param));
				result.append(F.Median(list));
				result.append(F.Quantile(list, F.C3D4, param));
				result.append(F.Max(list));

				return result;
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	private final static class FRatioDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				return
				// [$ Piecewise({{m/(-2 + m), m > 2}}, Indeterminate) $]
				F.Piecewise(F.List(F.List(F.Times(F.Power(F.Plus(F.CN2, m), -1), m), F.Greater(m, F.C2))),
						F.Indeterminate); // $$;
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// [$ (m*(-1 + 1/InverseBetaRegularized(1, -(1/2), m/2, n/2)))/n $]
				F.Times(m, F.Power(n, -1), F.Plus(F.CN1,
						F.Power(F.InverseBetaRegularized(F.C1, F.CN1D2, F.Times(F.C1D2, m), F.Times(F.C1D2, n)), -1))); // $$;
			}
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				IExpr function =
						// [$ Piecewise({{BetaRegularized((#*n)/(m + #*n), n/2, m/2), # > 0}}, 0) & $]
						F.Function(F.Piecewise(F.List(F.List(
								F.BetaRegularized(F.Times(n, F.Power(F.Plus(m, F.Times(F.Slot1, n)), -1), F.Slot1),
										F.Times(F.C1D2, n), F.Times(F.C1D2, m)),
								F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				IExpr function =
						// [$ Piecewise({{(#^(-1 + n/2)*m^(m/2)*n^(n/2)*(m + #*n)^((1/2)*(-m - n)))/Beta(n/2, m/2), # >
						// 0}}, 0) & $]
						F.Function(
								F.Piecewise(F.List(F.List(
										F.Times(F.Power(m, F.Times(F.C1D2, m)), F.Power(n, F.Times(F.C1D2, n)),
												F.Power(F.Plus(m, F.Times(F.Slot1, n)),
														F.Times(F.C1D2, F.Plus(F.Negate(m), F.Negate(n)))),
												F.Power(F.Beta(F.Times(F.C1D2, n), F.Times(F.C1D2, m)), -1),
												F.Power(F.Slot1, F.Plus(F.CN1, F.Times(F.C1D2, n)))),
										F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				return
				// [$ Piecewise({{(2*m^2*(-2 + m + n))/((-4 + m)*(-2 + m)^2*n), m > 4}}, Indeterminate) $]
				F.Piecewise(F.List(F.List(
						F.Times(F.C2, F.Sqr(m), F.Plus(F.CN2, m, n),
								F.Power(F.Times(F.Plus(F.CN4, m), F.Sqr(F.Plus(F.CN2, m)), n), -1)),
						F.Greater(m, F.C4))), F.Indeterminate); // $$;
			}
			return F.NIL;
		}
	}

	/**
	 * <pre>
	 * FrechetDistribution(a, b)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns a Frechet distribution.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:<br />
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Fr%C3%A9chet_distribution">Wikipedia - Frechet distribution</a></li>
	 * </ul>
	 * <h3>Related terms</h3>
	 * <p>
	 * <a href="CDF.md">CDF</a>, <a href="Mean.md">Mean</a>, <a href="Mean.md">Median</a>, <a href="PDF.md">PDF</a>,
	 * <a href="Quantile.md">Quantile</a>, <a href="StandardDeviation.md">StandardDeviation</a>,
	 * <a href="Variance.md">Variance</a>
	 * </p>
	 */
	private final static class FrechetDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance, IRandomVariate {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 3);
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// Piecewise({{m*Gamma(1 - 1/n), 1 < n}}, Infinity)
				return F.Piecewise(
						F.List(F.List(F.Times(m, F.Gamma(F.Subtract(F.C1, F.Power(n, F.CN1)))), F.Less(F.C1, n))),
						F.CInfinity);
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// (n,m) => m/Log(2)^n^(-1)
				return F.Times(m, F.Power(F.Log(F.C2), F.Negate(F.Power(n, -1))));
			}
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				IExpr function =
						// [$ (Piecewise({{E^(-(#/m)^(-n)), # > 0}}, 0)) & $]
						F.Function(F.Piecewise(
								F.List(F.List(F.Exp(F.Negate(F.Power(F.Times(F.Power(m, -1), F.Slot1), F.Negate(n)))),
										F.Greater(F.Slot1, F.C0))),
								F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				//
				IExpr function =
						// [$ Piecewise({{((#/m)^(-1 - n)*n)/(E^(#/m)^(-n)*m), # > 0}}, 0) & $]
						F.Function(F.Piecewise(F.List(F.List(
								F.Times(F.Power(
										F.Times(F.Exp(F.Power(F.Times(F.Power(m, -1), F.Slot1), F.Negate(n))), m), -1),
										n, F.Power(F.Times(F.Power(m, -1), F.Slot1), F.Plus(F.CN1, F.Negate(n)))),
								F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// Piecewise({{m^2*(Gamma(1 - 2/n) - Gamma(1 - 1/n)^2), n > 2}}, Infinity)
				return F.Piecewise(F.List(F.List(
						F.Times(F.Sqr(m),
								F.Plus(F.Gamma(F.Plus(F.C1, F.Times(F.CN2, F.Power(n, -1)))),
										F.Negate(F.Sqr(F.Gamma(F.Plus(F.C1, F.Negate(F.Power(n, -1)))))))),
						F.Greater(n, F.C2))), F.CInfinity);
			}
			return F.NIL;
		}

		@Override
		public IExpr randomVariate(Random random, IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				if (n.isReal() && m.isReal()) {
					// avoid result -Infinity when reference is close to 1.0
					double reference = random.nextDouble();
					double uniform = reference == NEXTDOWNONE ? reference : Math.nextUp(reference);
					uniform = -Math.log(uniform);
					return m.times(F.Power.of(F.num(uniform), n.reciprocal().negate()));
				}
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	private final static class GammaDistribution extends AbstractEvaluator
			implements IDistribution, IRandomVariate, IVariance, IPDF, ICDF {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			// 2 or 4 arguments
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				//
				IExpr a = dist.arg1();
				IExpr b = dist.arg2();
				IExpr function =
						// [$ (Piecewise({{GammaRegularized(a, 0, #/b), # > 0}}, 0)&) $]
						F.Function(
								F.Piecewise(F.List(F.List(F.GammaRegularized(a, F.C0, F.Times(F.Power(b, -1), F.Slot1)),
										F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			} else if (dist.isAST(F.GammaDistribution, 5)) {
				//
				IExpr a = dist.arg1();
				IExpr b = dist.arg2();
				IExpr g = dist.arg3();
				IExpr d = dist.arg4();
				IExpr function =
						// [$ (Piecewise({{GammaRegularized(a, 0, ((# - d)/b)^g), # > d}}, 0)&) $]
						F.Function(F.Piecewise(F.List(F.List(
								F.GammaRegularized(a, F.C0,
										F.Power(F.Times(F.Power(b, -1), F.Plus(F.Negate(d), F.Slot1)), g)),
								F.Greater(F.Slot1, d))), F.C0)); // $$;
				return callFunction(function, k);
			}

			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// m*n
				return F.Times(m, n);
			}
			if (dist.size() == 5) {
				IExpr a = dist.arg1();
				IExpr b = dist.arg2();
				IExpr g = dist.arg3();
				IExpr d = dist.arg4();
				return // [$ d + (b*Gamma(a + 1/g))/Gamma(a) $]
				F.Plus(d, F.Times(b, F.Power(F.Gamma(a), -1), F.Gamma(F.Plus(a, F.Power(g, -1))))); // $$;
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// (n,m) => m*InverseGammaRegularized(n, 0, 1/2)
				return F.Times(m, F.InverseGammaRegularized(n, F.C0, F.C1D2));
			}
			if (dist.size() == 5) {
				IExpr a = dist.arg1();
				IExpr b = dist.arg2();
				IExpr g = dist.arg3();
				IExpr d = dist.arg4();
				// (a,b,g,d) => d + b*InverseGammaRegularized(a, 1/2)^(1/g)
				return F.Plus(d, F.Times(b, F.Power(F.InverseGammaRegularized(a, F.C1D2), F.Power(g, -1))));
			}
			return F.NIL;
		}

		@Override
		public IExpr randomVariate(Random random, IAST dist) {
			if (dist.isAST2()) {
				//
				ISignedNumber a = dist.arg1().evalReal();
				ISignedNumber b = dist.arg2().evalReal();
				if (a != null && b != null) {
					RandomDataGenerator rdg = new RandomDataGenerator();
					return F.num(rdg.nextGamma(a.doubleValue(), b.doubleValue()));
				}
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// m^2*n
				return F.Times(F.Sqr(m), n);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				//
				IExpr a = dist.arg1();
				IExpr b = dist.arg2();
				IExpr function =
						// [$ ( Piecewise({{#^(-1 + a)/(b^a*E^(#/b)*Gamma(a)), # > 0}}, 0) & ) $]
						F.Function(
								F.Piecewise(F.List(F.List(
										F.Times(F.Power(F.Times(F.Power(b, a), F.Exp(F.Times(F.Power(b, -1), F.Slot1)),
												F.Gamma(a)), -1), F.Power(F.Slot1, F.Plus(F.CN1, a))),
										F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			} else if (dist.isAST(F.GammaDistribution, 5)) {
				//
				IExpr a = dist.arg1();
				IExpr b = dist.arg2();
				IExpr g = dist.arg3();
				IExpr d = dist.arg4();
				IExpr function =
						// [$ ( Piecewise( {{(((# - d)/b)^(-1 + a*g)*g)/(E^((# - d)/b)^g*(b*Gamma(a))), # > d}}, 0) & )
						// $]
						F.Function(F.Piecewise(F.List(F.List(F.Times(g,
								F.Power(F.Times(
										F.Exp(F.Power(F.Times(F.Power(b, -1), F.Plus(F.Negate(d), F.Slot1)), g)), b,
										F.Gamma(a)), -1),
								F.Power(F.Times(F.Power(b, -1), F.Plus(F.Negate(d), F.Slot1)),
										F.Plus(F.CN1, F.Times(a, g)))),
								F.Greater(F.Slot1, d))), F.C0)); // $$;
				return callFunction(function, k);
			}

			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	private static class GeometricMean extends AbstractFunctionEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			IAST arg1 = Validate.checkASTType(ast, 1);
			if (arg1.isRealVector()) {
				return F.num(StatUtils.geometricMean(arg1.toDoubleVector()));
			}
			if (arg1.size() > 1) {
				return F.Power(arg1.setAtClone(0, F.Times), F.fraction(1, arg1.argSize()));
			}
			return F.NIL;
		}

		@Override
		public IExpr numericEval(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);

			double[] values = ast.getAST(1).toDoubleVector();
			return F.num(StatUtils.geometricMean(values));
		}
	}

	private final static class GeometricDistribution extends AbstractEvaluator
			implements ICDF, IDiscreteDistribution, IPDF, IVariance {// , IRandomVariate

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST1()) {
				// -1 + 1/n
				IExpr n = dist.arg1();
				return F.Plus(F.CN1, F.Power(n, F.CN1));
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr n = dist.arg1();
				IExpr function =
						// [$ (Piecewise({{1 - (1 - n)^(1 + Floor(#)), # >= 0}}, 0)) & $]
						F.Function(
								F.Piecewise(F.List(F.List(
										F.Plus(F.C1,
												F.Negate(F.Power(F.Plus(F.C1, F.Negate(n)),
														F.Plus(F.C1, F.Floor(F.Slot1))))),
										F.GreaterEqual(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr n = dist.arg1();
				//
				IExpr function =
						// [$ (Piecewise({{(1 - n)^#*n, # >= 0}}, 0)) & $]
						F.Function(F.Piecewise(F.List(F.List(F.Times(F.Power(F.Plus(F.C1, F.Negate(n)), F.Slot1), n),
								F.GreaterEqual(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST1()) {
				// (1-n) / n^2
				IExpr n = dist.arg1();
				return F.Times(F.Subtract(F.C1, n), F.Power(n, F.CN2));
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	private final static class GumbelDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance, IRandomVariate {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			// 0 or 2 args
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST0()) {
				// -EulerGamma
				return F.EulerGamma.negate();
			}
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// -EulerGamma*m + n
				return F.Plus(F.Times(F.CN1, F.EulerGamma, m), n);
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST0()) {
				// ( ) => Log(Log(2))
				return F.Log(F.Log(F.C2));
			}
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// (n, m) => n + m*Log(Log(2))
				return F.Plus(n, F.Times(m, F.Log(F.Log(F.C2))));
			}
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				IExpr function =
						// [$ (1 - E^(-E^((# - n)/m))) & $]
						F.Function(F.Plus(F.C1, F.Negate(
								F.Exp(F.Negate(F.Exp(F.Times(F.Power(m, -1), F.Plus(F.Negate(n), F.Slot1)))))))); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				IExpr function =
						// [$ (E^(-E^((# - n)/m) + (# - n)/m)/m) & $]
						F.Function(F.Times(
								F.Exp(F.Plus(F.Negate(F.Exp(F.Times(F.Power(m, -1), F.Plus(F.Negate(n), F.Slot1)))),
										F.Times(F.Power(m, -1), F.Plus(F.Negate(n), F.Slot1)))),
								F.Power(m, -1))); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST2()) {
				IExpr m = dist.arg2();
				// (m^2*Pi^2)/6
				return F.Times(F.QQ(1, 6), F.Sqr(m), F.Sqr(F.Pi));
			}
			return F.NIL;
		}

		@Override
		public IExpr randomVariate(Random random, IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				if (n.isReal() && m.isReal()) {
					// avoid result -Infinity when reference is close to 1.0
					double reference = random.nextDouble();
					double uniform = reference == NEXTDOWNONE ? reference : Math.nextUp(reference);
					uniform = -Math.log(uniform);
					return m.add(n.times(F.Log(F.num(uniform))));
				}
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	private final static class HypergeometricDistribution extends AbstractEvaluator
			implements ICDF, IDiscreteDistribution, IPDF, IVariance {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 4);
			return F.NIL;
		}

		private int[] parameters(IAST hypergeometricDistribution) {
			if (hypergeometricDistribution.size() == 4) {
				int N = hypergeometricDistribution.arg1().toIntDefault(-1);
				int n = hypergeometricDistribution.arg2().toIntDefault(-1);
				int m_n = hypergeometricDistribution.arg3().toIntDefault(-1);
				if (N >= 0 && n >= 0 && m_n >= 0) {
					int param[] = new int[3];
					param[0] = N;
					param[1] = n;
					param[2] = m_n;
					return param;
				}
			}
			return null;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST3()) {
				int param[] = parameters(dist);
				if (param != null) {
					// N * (n / m_n)
					return F.ZZ(param[0]).multiply(F.QQ(param[1], param[2]));
				}
				IExpr N = dist.arg1();
				IExpr n = dist.arg2();
				IExpr m_n = dist.arg3();
				return F.Divide(F.Times(N, n), m_n);
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST3()) {
				IExpr n = dist.arg1();
				IExpr ns = dist.arg2();
				IExpr nt = dist.arg3();
				//
				IExpr function =
						// [$ Piecewise({{1 - (ns!*(-ns + nt)!*HypergeometricPFQRegularized({1, 1 - n + Floor(#), 1 - ns
						// + Floor(#)}, {2 + Floor(#), 2 - n - ns + nt + Floor(#)}, 1))/(Binomial(nt, n)*(-1 + n -
						// Floor(#))!*(-1 + ns - Floor(#))!), 0 <= # && n + ns - nt <= # && # < n && # < ns}, {1, # >= n
						// || # >= ns}}, 0) & $]
						F.Function(F.Piecewise(
								F.List(F.List(
										F.Plus(F.C1, F.Times(F.CN1, F.Factorial(ns),
												F.Factorial(F.Plus(F.Negate(ns), nt)),
												F.Power(F.Times(F.Binomial(nt, n),
														F.Factorial(F.Plus(F.CN1, n, F.Negate(F.Floor(F.Slot1)))),
														F.Factorial(F.Plus(F.CN1, ns, F.Negate(F.Floor(F.Slot1))))),
														-1),
												F.HypergeometricPFQRegularized(
														F.List(F.C1, F.Plus(F.C1, F.Negate(n), F.Floor(F.Slot1)),
																F.Plus(F.C1, F.Negate(ns), F.Floor(F.Slot1))),
														F.List(F.Plus(F.C2, F.Floor(F.Slot1)),
																F.Plus(F.C2, F.Negate(n), F.Negate(ns), nt,
																		F.Floor(F.Slot1))),
														F.C1))),
										F.And(F.LessEqual(F.C0, F.Slot1),
												F.LessEqual(F.Plus(n, ns, F.Negate(nt)), F.Slot1), F.Less(F.Slot1, n),
												F.Less(F.Slot1, ns))),
										F.List(F.C1, F.Or(F.GreaterEqual(F.Slot1, n), F.GreaterEqual(F.Slot1, ns)))),
								F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST3()) {
				IExpr n = dist.arg1();
				IExpr ns = dist.arg2();
				IExpr nt = dist.arg3();
				IExpr function =
						// [$ (Piecewise({{(Binomial(ns, #)*Binomial(-ns + nt, -# + n))/Binomial(nt, n), 0 <= # <= n &&
						// n + ns - nt <= # <= n && 0 <= # <= ns && n + ns - nt <= # <= ns}}, 0)) & $]
						F.Function(
								F.Piecewise(
										F.List(F.List(
												F.Times(F.Binomial(ns, F.Slot1), F.Power(F.Binomial(nt, n), -1),
														F.Binomial(F.Plus(F.Negate(ns), nt),
																F.Plus(F.Negate(F.Slot1), n))),
												F.And(F.LessEqual(F.C0, F.Slot1, n),
														F.LessEqual(F.Plus(n, ns, F.Negate(nt)), F.Slot1, n),
														F.LessEqual(F.C0, F.Slot1, ns),
														F.LessEqual(F.Plus(n, ns, F.Negate(nt)), F.Slot1, ns)))),
										F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST3()) {
				int param[] = parameters(dist);
				if (param != null) {
					int N = param[0];
					int n = param[1];
					int m_n = param[2];
					IFraction rd1 = F.QQ(m_n - n, m_n);
					IFraction rd2 = F.QQ(m_n - N, m_n);
					IFraction rd3 = F.QQ(N, m_n - 1);
					IFraction rd4 = F.QQ(n, 1);
					return rd1.multiply(rd2).multiply(rd3).multiply(rd4);

				}
				IExpr N = dist.arg1();
				IExpr n = dist.arg2();
				IExpr mn = dist.arg3();
				// (n*(1 - n/m_n)*(m_n - N)*N)/((-1 + m_n)*m_n)
				return F.Times(F.Power(F.Plus(F.CN1, mn), -1), F.Power(mn, -1), n,
						F.Plus(F.C1, F.Times(F.CN1, F.Power(mn, -1), n)), F.Plus(mn, F.Negate(N)), N);
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	/**
	 * <pre>
	 * Covariance(a, b)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * computes the covariance between the equal-sized vectors <code>a</code> and <code>b</code>.
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Covariance({0.2, 0.3, 0.1}, {0.3, 0.3, -0.2})
	 * 0.025
	 * </pre>
	 */
	private final static class Covariance extends AbstractMatrix1Expr {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkRange(ast, 2, 3);
			if (ast.size() == 2) {
				return super.evaluate(ast, engine);
			}

			if (ast.size() == 3) {
				final IAST arg1 = (IAST) ast.arg1();
				final IAST arg2 = (IAST) ast.arg2();
				return evaluateArg2(arg1, arg2, engine);
			}
			return F.NIL;
		}

		private IExpr evaluateArg2(final IAST arg1, final IAST arg2, EvalEngine engine) {
			try {
				int arg1Length = arg1.isVector();
				if (arg1Length > 1) {
					int arg2Length = arg2.isVector();
					if (arg1Length == arg2Length) {
						try {
							double[] arg1DoubleArray = arg1.toDoubleVector();
							double[] arg2DoubleArray = arg2.toDoubleVector();
							org.hipparchus.stat.correlation.Covariance cov = new org.hipparchus.stat.correlation.Covariance();
							return F.num(cov.covariance(arg1DoubleArray, arg2DoubleArray, true));
						} catch (Exception ex) {
							//
						}
						return vectorCovarianceSymbolic(arg1, arg2, arg1Length);
					}
				}
			} catch (final WrongArgumentType e) {
				// WrongArgumentType occurs in list2RealMatrix(),
				// if the matrix elements aren't pure numerical values
			} catch (final IndexOutOfBoundsException e) {
				if (Config.SHOW_STACKTRACE) {
					e.printStackTrace();
				}
			}
			return F.NIL;
		}

		public static IExpr vectorCovarianceSymbolic(final IAST arg1, final IAST arg2, int arg1Length) {
			if (arg1Length == 2) {
				return F.Times(F.C1D2, F.Subtract(arg1.arg1(), arg1.arg2()),
						F.Subtract(F.Conjugate(arg2.arg1()), F.Conjugate(arg2.arg2())));
			}
			IAST num1 = arg1.apply(F.Plus);
			IExpr factor = F.integer(-1 * (arg1.size() - 2));
			IASTAppendable v1 = F.PlusAlloc(arg1.size());
			v1.appendArgs(arg1.size(),
					i -> F.Times(F.CN1, num1.setAtClone(i, F.Times(factor, arg1.get(i))), F.Conjugate(arg2.get(i))));
			return F.Divide(v1, F.integer((arg1.argSize()) * (arg1.size() - 2)));
		}

		@Override
		public IExpr matrixEval(FieldMatrix<IExpr> matrix) {
			return F.NIL;
		}

		@Override
		public IExpr numericEval(final IAST ast, EvalEngine engine) {
			Validate.checkRange(ast, 2, 3);
			if (ast.size() == 2) {
				return super.numericEval(ast, engine);
			}
			if (ast.size() == 3) {
				final IAST arg1 = (IAST) ast.arg1();
				final IAST arg2 = (IAST) ast.arg2();
				return evaluateArg2(arg1, arg2, engine);
			}
			return F.NIL;
		}

		@Override
		public IExpr realMatrixEval(RealMatrix matrix) {
			org.hipparchus.stat.correlation.Covariance cov = new org.hipparchus.stat.correlation.Covariance(matrix);
			return new ASTRealMatrix(cov.getCovarianceMatrix(), false);
		}
	}

	/**
	 * <pre>
	 * DiscreteUniformDistribution({min, max})
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns a discrete uniform distribution.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:<br />
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Discrete_uniform_distribution">Wikipedia - Discrete uniform
	 * distribution</a></li>
	 * </ul>
	 * <h3>Related terms</h3>
	 * <p>
	 * <a href="CDF.md">CDF</a>, <a href="Mean.md">Mean</a>, <a href="Mean.md">Median</a>, <a href="PDF.md">PDF</a>,
	 * <a href="Quantile.md">Quantile</a>, <a href="StandardDeviation.md">StandardDeviation</a>,
	 * <a href="Variance.md">Variance</a>
	 * </p>
	 */
	private final static class DiscreteUniformDistribution extends AbstractEvaluator
			implements IDiscreteDistribution, IVariance, ICDF, IPDF, IRandomVariate {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				// (max + min)/2
				return F.Times(F.C1D2, F.Plus(minMax[0], minMax[1]));
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				IExpr l = minMax[0];
				IExpr r = minMax[1];
				// (l,r) => -1 + l + Max(1, Ceiling((1/2)*(1 - l + r)))
				return F.Plus(F.CN1, l, F.Max(F.C1, F.Ceiling(F.Times(F.C1D2, F.Plus(F.C1, F.Negate(l), r)))));
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				// (1/12)*(-1+(1+max-min)^2)
				return F.Times(F.QQ(1L, 12L), F.Plus(F.CN1, F.Sqr(F.Plus(F.C1, minMax[1], F.Negate(minMax[0])))));
			}

			return F.NIL;
		}

		public IExpr[] minmax(IAST dist) {
			if (dist.size() == 2 && dist.arg1().isList()) {
				IAST list = (IAST) dist.arg1();
				if (list.isAST2()) {
					IExpr min = list.arg1();
					IExpr max = list.arg2();
					return new IExpr[] { min, max };
				}
			}
			return null;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				IExpr a = minMax[0];
				IExpr b = minMax[1];
				IExpr function =
						// [$ (Piecewise({{(1 - a + Floor(#))/(1 - a + b), a<=#<b}, {1, # >= b}}, 0)) & $]
						F.Function(F.Piecewise(F.List(
								F.List(F.Times(F.Power(F.Plus(F.C1, F.Negate(a), b), -1),
										F.Plus(F.C1, F.Negate(a), F.Floor(F.Slot1))),
										F.And(F.LessEqual(a, F.Slot1), F.Less(F.Slot1, b))),
								F.List(F.C1, F.GreaterEqual(F.Slot1, b))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				IExpr a = minMax[0];
				IExpr b = minMax[1];
				IExpr function =
						// [$ ( Piecewise({{1/(1 - a + b), a <= # <= b}}, 0) & ) $]
						F.Function(F.Piecewise(
								F.List(F.List(F.Power(F.Plus(F.C1, F.Negate(a), b), -1), F.LessEqual(a, F.Slot1, b))),
								F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr randomVariate(Random random, IAST dist) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				int min = minMax[0].toIntDefault(Integer.MIN_VALUE);
				int max = minMax[1].toIntDefault(Integer.MIN_VALUE);
				if (min < max && min != Integer.MIN_VALUE) {
					return F.ZZ(new DiscreteUniformGenerator(min, max, random).nextValue());
				}
			}
			return F.NIL;
		}
	}

	/**
	 * <pre>
	 * ErlangDistribution({k, lambda})
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns a Erlang distribution.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:<br />
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Erlang_distribution">Wikipedia - Erlang distribution</a></li>
	 * </ul>
	 * <h3>Related terms</h3>
	 * <p>
	 * <a href="CDF.md">CDF</a>, <a href="Mean.md">Mean</a>, <a href="Mean.md">Median</a>, <a href="PDF.md">PDF</a>,
	 * <a href="Quantile.md">Quantile</a>, <a href="StandardDeviation.md">StandardDeviation</a>,
	 * <a href="Variance.md">Variance</a>
	 * </p>
	 */
	private final static class ErlangDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 3);
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST2()) {
				// n/m
				return F.Divide(dist.arg1(), dist.arg2());
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// (n,m) => InverseGammaRegularized(n, 0, 1/2)/m
				return F.Times(F.Power(m, -1), F.InverseGammaRegularized(n, F.C0, F.C1D2));
			}
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				IExpr function =
						// [$ (Piecewise({{GammaRegularized(n, 0, #*m), # > 0}}, 0)) & $]
						F.Function(F.Piecewise(F.List(
								F.List(F.GammaRegularized(n, F.C0, F.Times(F.Slot1, m)), F.Greater(F.Slot1, F.C0))),
								F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				//
				IExpr function =
						// [$ Piecewise({{(#^(-1 + n)*m^n)/(E^(#*m)*Gamma(n)), # > 0}}, 0) & $]
						F.Function(F.Piecewise(F.List(F.List(
								F.Times(F.Power(m, n), F.Power(F.Times(F.Exp(F.Times(F.Slot1, m)), F.Gamma(n)), -1),
										F.Power(F.Slot1, F.Plus(F.CN1, n))),
								F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST2()) {
				// n/(m^2)
				return F.Divide(dist.arg1(), F.Sqr(dist.arg2()));
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	/**
	 * <pre>
	 * Expectation(pure - function, data - set)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the expected value of the <code>pure-function</code> for the given <code>data-set</code>.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Expected_value">Wikipedia - Expected value</a></li>
	 * </ul>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Expectation((#^3)&amp;, {a,b,c}) 
	 * 1/3*(a^3+b^3+c^3) 
	 * 
	 * &gt;&gt; Expectation(2*x+3,Distributed(x,{a,b,c,d})) 
	 * 1/4*(12+2*a+2*b+2*c+2*d)
	 * </pre>
	 */
	private static class Expectation extends AbstractFunctionEvaluator {
		// static final double CDF_NUMERIC_THRESHOLD = Config.DOUBLE_EPSILON;
		//
		// static boolean isFinished(IExpr p_equals, IExpr cumprob) {
		// boolean finished = false;
		// finished |= cumprob.isOne();
		// finished |= // !ExactScalarQ.of(cumprob) && //
		// p_equals.isZero() && //
		// F.isZero(cumprob.subtract(F.C1).abs().evalDouble(), CDF_NUMERIC_THRESHOLD);
		// return finished;
		// }
		//
		// private static IExpr expect(Function<IExpr, IExpr> function, IAST distribution,
		// IDiscreteDistribution discreteDistribution) {
		// IExpr value = null;
		// IExpr p_equals = F.C0;
		// IExpr cumprob = F.C0;
		// int sample = discreteDistribution.getSupportLowerBound(distribution);
		// while (!isFinished(p_equals, cumprob)) {
		// IExpr x = F.QQ(sample, 1);
		// p_equals = discreteDistribution.pEquals(sample, distribution);
		// cumprob = cumprob.add(p_equals);
		// IExpr delta = function.apply(x).multiply(p_equals);
		// value = Objects.isNull(value) ? delta : value.add(delta);
		// ++sample;
		// }
		// return value;
		// }

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {

			if (ast.size() == 3) {
				try {
					IExpr xExpr = ast.arg1();
					if (xExpr.isFunction() && ast.arg2().isList()) {
						IAST data = (IAST) ast.arg2();
						IASTAppendable sum = F.PlusAlloc(data.size());
						for (int i = 1; i < data.size(); i++) {
							sum.append(F.unaryAST1(xExpr, data.get(i)));
						}
						return sum.divide(F.ZZ(data.argSize()));
						// int sum = 0;
						// for (int i = 1; i < data.size(); i++) {
						// if (engine.evalTrue(F.unaryAST1(predicate, data.get(i)))) {
						// sum++;
						// }
						// }
						// return F.QQ(sum, data.argSize());
					}
					if (ast.arg2().isAST(F.Distributed, 3)) {
						IExpr x = ast.arg2().first();
						IExpr distribution = ast.arg2().second();
						if (distribution.isList()) {
							//
							IAST data = (IAST) distribution;

							// Sum( predicate , data ) / data.argSize()
							IASTAppendable sum = F.PlusAlloc(data.size());
							for (int i = 1; i < data.size(); i++) {
								sum.append(F.subst(xExpr, F.Rule(x, data.get(i))));
							}
							return sum.divide(F.ZZ(data.argSize()));
							// } else if (distribution.isDiscreteDistribution()) {
							// IDiscreteDistribution dist = getDiscreteDistribution(distribution);
							// int[] interval = new int[] { dist.getSupportLowerBound(distribution),
							// dist.getSupportUpperBound(distribution) };
							// // int[] interval = dist.range(distribution, xExpr, x);
							// if (interval != null) {
							// IExpr pdf = F.PDF.of(engine, distribution, x);
							// // for discrete distributions take the sum:
							// IASTAppendable sum = F.PlusAlloc(100);
							//
							// for (int i = interval[0]; i <= interval[1]; i++) {
							// IExpr temp = engine.evaluate(F.subst(pdf, F.Rule(x, F.ZZ(i))));
							// if (!temp.isZero()) {
							// sum.append( F.Times(F.subst(xExpr, F.Rule(x, F.ZZ(i))), temp) );
							// }
							// }
							// return sum;
							// }
						}
					}
				} catch (Exception ex) {
					if (Config.SHOW_STACKTRACE) {
						ex.printStackTrace();
					}
				}
			}
			return F.NIL;
		}

	}

	private final static class ExponentialDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance, IRandomVariate {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST1()) {
				// 1/x
				return F.Power(dist.arg1(), F.CN1);
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST1()) {
				// Log(2)/x
				return F.Times(F.Log(F.C2), F.Power(dist.arg1(), F.CN1));
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST1()) {
				return F.Power(dist.arg1(), F.CN2);
			}
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr n = dist.arg1();
				IExpr function =
						// [$ (Piecewise({{1 - E^((-#)*n), # >= 0}}, 0)) & $]
						F.Function(F.Piecewise(F.List(F.List(F.Plus(F.C1, F.Negate(F.Exp(F.Times(F.CN1, F.Slot1, n)))),
								F.GreaterEqual(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
				// Piecewise({{1 - E^((-k)*n), k >= 0}}, 0)
				// return F.Piecewise(F.List(
				// F.List(F.Plus(F.C1, F.Negate(F.Power(F.E, F.Times(F.CN1, k, n)))), F.GreaterEqual(k, F.C0))),
				// F.C0);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr n = dist.arg1();
				//
				IExpr function =
						// [$ Piecewise({{n/E^(#*n), # >= 0}}, 0) & $]
						F.Function(F.Piecewise(F.List(F.List(F.Times(F.Power(F.Exp(F.Times(F.Slot1, n)), -1), n),
								F.GreaterEqual(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr randomVariate(Random random, IAST dist) {
			if (dist.isAST1()) {
				if (dist.arg1().isReal() && dist.arg1().isPositiveResult()) {
					double rate = dist.arg1().evalDouble();
					return F.num(new ExponentialGenerator(rate, random).nextValue());
				}
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	// private final static class KolmogorovSmirnovTest extends AbstractFunctionEvaluator {
	//
	// @Override
	// public IExpr evaluate(final IAST ast, EvalEngine engine) {
	// if (ast.isAST1()) {
	// double[] arg1Values = ast.arg1().toDoubleVector();
	// org.hipparchus.stat.inference.KolmogorovSmirnovTest test = new
	// org.hipparchus.stat.inference.KolmogorovSmirnovTest();
	// double d = test.kolmogorovSmirnovTest(arg1Values, arg1Values);
	// return F.num(d);
	//
	// } else if (ast.isAST2()) {
	// int len1 = ast.arg1().isVector();
	// if (len1 > 0) {
	// double[] arg1Values = ast.arg1().toDoubleVector();
	// if (arg1Values != null) {
	// int len2 = ast.arg2().isVector();
	// if (len2 > 0) {
	// double[] arg2Values = ast.arg2().toDoubleVector();
	// if (arg2Values != null) {
	// org.hipparchus.stat.inference.KolmogorovSmirnovTest test = new
	// org.hipparchus.stat.inference.KolmogorovSmirnovTest();
	// double d = test.kolmogorovSmirnovTest(arg1Values, arg2Values);
	// return F.num(d);
	// }
	// return F.NIL;
	// }
	// IExpr head = ast.arg2().head();
	// if (head instanceof IBuiltInSymbol) {
	// IEvaluator evaluator = ((IBuiltInSymbol) head).getEvaluator();
	// if (evaluator instanceof IDistribution) {
	// RealDistribution dist = ((IDistribution) evaluator).dist();
	// if (dist != null) {
	// org.hipparchus.stat.inference.KolmogorovSmirnovTest test = new
	// org.hipparchus.stat.inference.KolmogorovSmirnovTest();
	// double d = test.kolmogorovSmirnovTest(dist, arg1Values);
	// return F.num(d);
	// }
	// }
	// }
	// }
	// }
	// }
	// return F.NIL;
	// }
	//
	// }

	/**
	 * <pre>
	 * Kurtosis(list)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * gives the Pearson measure of kurtosis for <code>list</code> (a measure of existing outliers).
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Kurtosis({1.1, 1.2, 1.4, 2.1, 2.4})
	 * 1.42098
	 * </pre>
	 */
	private final static class Kurtosis extends AbstractEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			if (ast.arg1().isList()) {
				IAST list = (IAST) ast.arg1();
				return F.Divide(F.CentralMoment(list, F.C4), F.Power(F.CentralMoment(list, F.C2), F.C2));
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	/**
	 * <pre>
	 * LogNormalDistribution(m, s)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns a log-normal distribution.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:<br />
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Log-normal_distribution">Wikipedia - Log-normal distribution</a></li>
	 * </ul>
	 * <h3>Related terms</h3>
	 * <p>
	 * <a href="CDF.md">CDF</a>, <a href="Mean.md">Mean</a>, <a href="Mean.md">Median</a>, <a href="PDF.md">PDF</a>,
	 * <a href="Quantile.md">Quantile</a>, <a href="StandardDeviation.md">StandardDeviation</a>,
	 * <a href="Variance.md">Variance</a>
	 * </p>
	 */
	private final static class LogNormalDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			// Validate.checkSize(ast, 3);
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST2()) {
				IExpr m = dist.arg1();
				IExpr s = dist.arg2();
				// (m,s) -> E^(m+s^2/2)
				return F.Power(F.E, F.Plus(m, F.Times(F.C1D2, F.Sqr(s))));
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST2()) {
				// (m,s) -> E^(m+s^2/2)
				return F.Power(F.E, dist.arg1());
			}
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				//
				IExpr function =
						// [$ Piecewise({{(1/2)*Erfc((n - Log(#))/(Sqrt(2)*m)), # > 0}}, 0) & $]
						F.Function(
								F.Piecewise(F.List(F.List(
										F.Times(F.C1D2,
												F.Erfc(F.Times(F.Power(F.Times(F.CSqrt2, m), -1),
														F.Plus(n, F.Negate(F.Log(F.Slot1)))))),
										F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				//
				IExpr function =
						// [$ (Piecewise({{1/(E^((-n + Log(#))^2/(2*m^2))*(#*m*Sqrt(2*Pi))), # > 0}}, 0)) & $]
						F.Function(
								F.Piecewise(
										F.List(F.List(
												F.Power(F.Times(
														F.Exp(F.Times(F.Power(F.Times(F.C2, F.Sqr(m)), -1),
																F.Sqr(F.Plus(F.Negate(n), F.Log(F.Slot1))))),
														F.Slot1, m, F.Sqrt(F.Times(F.C2, F.Pi))), -1),
												F.Greater(F.Slot1, F.C0))),
										F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST2()) {
				IExpr m = dist.arg1();
				IExpr s = dist.arg2();
				// E^(2*m+s^2)*(-1+E^(s^2))
				return F.Times(F.Plus(F.CN1, F.Power(F.E, F.Sqr(s))), F.Power(F.E, F.Plus(F.Times(F.C2, m), F.Sqr(s))));
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	/**
	 * <pre>
	 * Mean(list)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the statistical mean of <code>list</code>.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Mean">Wikipedia - Mean</a></li>
	 * </ul>
	 * <p>
	 * <code>Mean</code> can be applied to the following distributions:
	 * </p>
	 * <blockquote>
	 * <p>
	 * <a href="BernoulliDistribution.md">BernoulliDistribution</a>,
	 * <a href="BinomialDistribution.md">BinomialDistribution</a>,
	 * <a href="DiscreteUniformDistribution.md">DiscreteUniformDistribution</a>,
	 * <a href="ErlangDistribution.md">ErlangDistribution</a>,
	 * <a href="ExponentialDistribution.md">ExponentialDistribution</a>,
	 * <a href="FrechetDistribution.md">FrechetDistribution</a>, <a href="GammaDistribution.md">GammaDistribution</a>,
	 * <a href="GeometricDistribution.md">GeometricDistribution</a>,
	 * <a href="GumbelDistribution.md">GumbelDistribution</a>,
	 * <a href="HypergeometricDistribution.md">HypergeometricDistribution</a>,
	 * <a href="LogNormalDistribution.md">LogNormalDistribution</a>,
	 * <a href="NakagamiDistribution.md">NakagamiDistribution</a>,
	 * <a href="NormalDistribution.md">NormalDistribution</a>, <a href="PoissonDistribution.md">PoissonDistribution</a>,
	 * <a href="StudentTDistribution.md">StudentTDistribution</a>,
	 * <a href="WeibullDistribution.md">WeibullDistribution</a>
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Mean({26, 64, 36})
	 * 42
	 * 
	 * &gt;&gt; Mean({1, 1, 2, 3, 5, 8})
	 * 10/3
	 * 
	 * &gt;&gt; Mean({a, b})
	 * 1/2*(a+b)
	 * </pre>
	 * <p>
	 * The <a href="https://en.wikipedia.org/wiki/Mean">mean</a> of the normal distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; Mean(NormalDistribution(m, s))
	 * m
	 * </pre>
	 */
	private final static class Mean extends AbstractTrigArg1 {

		@Override
		public IExpr evaluateArg1(final IExpr arg1) {
			try {
				if (arg1.isRealVector()) {
					return F.num(StatUtils.mean(arg1.toDoubleVector()));
				}
				if (arg1.isList()) {
					final IAST list = (IAST) arg1;
					return F.Times(list.apply(F.Plus), F.Power(F.integer(list.argSize()), F.CN1));
				}

				if (arg1.isDistribution()) {
					return getDistribution(arg1).mean((IAST) arg1);
				}
			} catch (Exception ex) {
				if (Config.SHOW_STACKTRACE) {
					ex.printStackTrace();
				}
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
			newSymbol.setAttributes(ISymbol.NOATTRIBUTE);
		}

	}

	private final static class MeanDeviation extends AbstractFunctionEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);

			int[] dim = ast.arg1().isMatrix();
			if (dim == null && ast.arg1().isListOfLists()) {
				return F.NIL;
			}
			if (dim != null) {
				IAST matrix = (IAST) ast.arg1();
				return matrix.mapMatrixColumns(dim, x -> F.MeanDeviation(x));
			}

			int length = ast.arg1().isVector();
			if (length > 0) {
				IAST vector = (IAST) ast.arg1();
				int size = vector.size();
				IASTAppendable sum = F.PlusAlloc(size);
				final IExpr mean = F.eval(F.Mean(F.Negate(vector)));
				vector.forEach(x -> sum.append(F.Abs(F.Plus(x, mean))));
				return F.Times(F.Power(F.ZZ(size - 1), -1), sum);
			}

			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
			newSymbol.setAttributes(ISymbol.NOATTRIBUTE);
		}

	}

	/**
	 * <pre>
	 * Median(list)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the median of <code>list</code>.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Median">Wikipedia - Median</a></li>
	 * </ul>
	 * <p>
	 * <code>Median</code> can be applied to the following distributions:
	 * </p>
	 * <blockquote>
	 * <p>
	 * <a href="BernoulliDistribution.md">BernoulliDistribution</a>,
	 * <a href="BinomialDistribution.md">BinomialDistribution</a>,
	 * <a href="DiscreteUniformDistribution.md">DiscreteUniformDistribution</a>,
	 * <a href="ErlangDistribution.md">ErlangDistribution</a>,
	 * <a href="ExponentialDistribution.md">ExponentialDistribution</a>,
	 * <a href="FrechetDistribution.md">FrechetDistribution</a>, <a href="GammaDistribution.md">GammaDistribution</a>,
	 * <a href="GeometricDistribution.md">GeometricDistribution</a>,
	 * <a href="GumbelDistribution.md">GumbelDistribution</a>,
	 * <a href="HypergeometricDistribution.md">HypergeometricDistribution</a>,
	 * <a href="LogNormalDistribution.md">LogNormalDistribution</a>,
	 * <a href="NakagamiDistribution.md">NakagamiDistribution</a>,
	 * <a href="NormalDistribution.md">NormalDistribution</a>,
	 * <a href="StudentTDistribution.md">StudentTDistribution</a>,
	 * <a href="WeibullDistribution.md">WeibullDistribution</a>
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Median({26, 64, 36})
	 * 36
	 * </pre>
	 * <p>
	 * For lists with an even number of elements, Median returns the mean of the two middle values:
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; Median({-11, 38, 501, 1183})
	 * 539/2
	 * </pre>
	 * <p>
	 * Passing a matrix returns the medians of the respective columns:
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; Median({{100, 1, 10, 50}, {-1, 1, -2, 2}})
	 * {99/2,1,4,26}
	 * 
	 * &gt;&gt; Median(LogNormalDistribution(m,s))
	 * E^m
	 * </pre>
	 */
	private final static class Median extends AbstractTrigArg1 {

		@Override
		public IExpr evaluateArg1(final IExpr arg1) {
			if (arg1.isRealVector()) {
				return F.num(StatUtils.percentile(arg1.toDoubleVector(), 50));
			}
			int[] dim = arg1.isMatrix();
			if (dim == null && arg1.isListOfLists()) {
				return F.NIL;
			}
			if (dim != null) {
				IAST matrix = (IAST) arg1;
				return matrix.mapMatrixColumns(dim, x -> F.Median(x));
			}
			if (arg1.isList()) {
				final IAST list = (IAST) arg1;
				if (list.size() > 1) {
					final IAST sortedList = EvalAttributes.copySortLess(list);
					int size = sortedList.size();
					if ((size & 0x00000001) == 0x00000001) {
						// odd number of elements
						size = size / 2;
						return F.Times(F.Plus(sortedList.get(size), sortedList.get(size + 1)), F.C1D2);
					} else {
						return sortedList.get(size / 2);
					}
				}
			}

			if (arg1.isDistribution()) {
				return getDistribution(arg1).median((IAST) arg1);
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
			newSymbol.setAttributes(ISymbol.NOATTRIBUTE);
		}
	}

	private final static class NakagamiDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			// Validate.checkSize(ast, 3);
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// (n,m) -> (Sqrt(m)*Pochhammer(n,1/2))/Sqrt(n)
				return F.Divide(F.Times(F.Sqrt(m), F.Pochhammer(n, F.C1D2)), F.Sqrt(n));
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// (n,m) -> Sqrt((m*InverseGammaRegularized(n, 0, 1/2))/n)
				return F.Sqrt(F.Times(m, F.Power(n, -1), F.InverseGammaRegularized(n, F.C0, F.C1D2)));
			}
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				IExpr function =
						// [$ Piecewise({{GammaRegularized(n, 0, (#^2*n)/m), # > 0}}, 0) & $]
						F.Function(F.Piecewise(
								F.List(F.List(F.GammaRegularized(n, F.C0, F.Times(F.Power(m, -1), n, F.Sqr(F.Slot1))),
										F.Greater(F.Slot1, F.C0))),
								F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				//
				IExpr function =
						// [$ (Piecewise({{(2*#^(-1 + 2*n)*(n/m)^n)/(E^((#^2*n)/m)*Gamma(n)), # > 0}}, 0)) & $]
						F.Function(
								F.Piecewise(F.List(F.List(
										F.Times(F.C2, F.Power(F.Times(F.Power(m, -1), n), n),
												F.Power(F.Times(F.Exp(F.Times(F.Power(m, -1), n, F.Sqr(F.Slot1))),
														F.Gamma(n)), -1),
												F.Power(F.Slot1, F.Plus(F.CN1, F.Times(F.C2, n)))),
										F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST2()) {

				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// m - (m*Pochhammer(n, 1/2)^2)/n
				return F.Subtract(m, F.Divide(F.Times(m, F.Sqr(F.Pochhammer(n, F.C1D2))), n));
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	/**
	 * <pre>
	 * NormalDistribution(m, s)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the normal distribution of mean <code>m</code> and sigma <code>s</code>.
	 * </p>
	 * </blockquote>
	 * 
	 * <pre>
	 * NormalDistribution()
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the standard normal distribution for <code>m = 0</code> and <code>s = 1</code>.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:<br />
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Normal_distribution">Wikipedia - Normal distribution</a></li>
	 * </ul>
	 * <h3>Examples</h3>
	 * <p>
	 * The <a href="https://en.wikipedia.org/wiki/Probability_density">probability density function</a> of the normal
	 * distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; PDF(NormalDistribution(m, s), x)
	 * 1/(Sqrt(2)*E^((-m+x)^2/(2*s^2))*Sqrt(Pi)*s)
	 * </pre>
	 * <p>
	 * The <a href="https://en.wikipedia.org/wiki/Cumulative_distribution_function">cumulative distribution function</a>
	 * of the standard normal distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; CDF(NormalDistribution( ), x)
	 * 1/2*(2-Erfc(x/Sqrt(2)))
	 * </pre>
	 * <p>
	 * The <a href="https://en.wikipedia.org/wiki/Mean">mean</a> of the normal distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; Mean(NormalDistribution(m, s))
	 * m
	 * </pre>
	 * <p>
	 * The <a href="https://en.wikipedia.org/wiki/Standard_deviation">standard deviation</a> of the normal distribution
	 * is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; StandardDeviation(NormalDistribution(m, s))
	 * s
	 * </pre>
	 * <p>
	 * The <a href="https://en.wikipedia.org/wiki/Variance">variance</a> of the normal distribution is
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; Variance(NormalDistribution(m, s))
	 * s^2
	 * </pre>
	 * <p>
	 * The <a href="https://en.wikipedia.org/wiki/Normal_distribution#Generating_values_from_normal_distribution">random
	 * variates</a> of a normal distribution can be generated with function <code>RandomVariate</code>
	 * </p>
	 * 
	 * <pre>
	 * &gt;&gt; RandomVariate(NormalDistribution(2,3), 10^1)
	 * {1.14364,6.09674,5.16495,2.39937,-0.52143,-1.46678,3.60142,-0.85405,2.06373,-0.29795}
	 * </pre>
	 */
	private final static class NormalDistribution extends AbstractEvaluator
			implements IDistribution, IVariance, IRandomVariate, IPDF, ICDF {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			// 0 or 2 args are allowed
			return F.NIL;
		}

		public RealDistribution dist() {
			return new org.hipparchus.distribution.continuous.NormalDistribution(0, 1);
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST0()) {
				return F.C0;
			}
			if (dist.isAST2()) {
				return dist.arg1();
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST0()) {
				return F.C0;
			}
			if (dist.isAST2()) {
				return dist.arg1();
			}
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST0()) {
				IExpr function =
						// [$ ( (1/2)*Erfc(-(#/Sqrt(2))) & ) $]
						F.Function(F.Times(F.C1D2, F.Erfc(F.Times(F.CN1, F.C1DSqrt2, F.Slot1)))); // $$;
				return callFunction(function, k);
			} else if (dist.isAST2()) {
				//
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				IExpr function =
						// [$ ( (1/2)*Erfc((-# + n)/(Sqrt(2)*m)) &) $]
						F.Function(F.Times(F.C1D2,
								F.Erfc(F.Times(F.Power(F.Times(F.CSqrt2, m), -1), F.Plus(F.Negate(F.Slot1), n))))); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST0()) {
				IExpr function =
						// [$ ( 1/(E^(#^2/2)*Sqrt(2*Pi)) & ) $]
						F.Function(F.Power(F.Times(F.Exp(F.Times(F.C1D2, F.Sqr(F.Slot1))), F.Sqrt(F.Times(F.C2, F.Pi))),
								-1)); // $$;
				return callFunction(function, k);
			} else if (dist.isAST2()) {
				//
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				IExpr function =
						// [$ ( 1/(E^((# - n)^2/(2*m^2))*(m*Sqrt(2*Pi))) & ) $]
						F.Function(F.Power(F.Times(F.Exp(
								F.Times(F.Power(F.Times(F.C2, F.Sqr(m)), -1), F.Sqr(F.Plus(F.Negate(n), F.Slot1)))), m,
								F.Sqrt(F.Times(F.C2, F.Pi))), -1)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST0()) {
				return F.C1;
			}
			if (dist.isAST2()) {
				return F.Sqr(dist.arg2());
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

		@Override
		public IExpr randomVariate(Random random, IAST dist) {
			if (dist.isAST0()) {
				return F.num(random.nextGaussian());
			}
			// return protected_quantile(dist, F.num(ThreadLocalRandom.current().nextDouble()));
			if (dist.isAST2()) {
				if (dist.arg1().isReal() && dist.arg1().isPositiveResult()) {
					double mean = dist.arg1().evalDouble();
					double sigma = dist.arg2().evalDouble();
					return F.num(new GaussianGenerator(mean, sigma, random).nextValue());
				}
			}
			return F.NIL;
		}

	}

	/**
	 * <pre>
	 * Probability(pure - function, data - set)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the probability of the <code>pure-function</code> for the given <code>data-set</code>.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Probability">Wikipedia - Probability</a></li>
	 * </ul>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Probability(#^2 + 3*# &lt; 11 &amp;, {-0.21848,1.67503,0.78687,4.9887,7.06587,-1.27856,0.79225,-0.01164,2.48227,-0.07223})
	 * 7/10
	 * 
	 * &gt;&gt; Probability(x^2 + 3*x &lt; 11,Distributed(x,{-0.21848,1.67503,0.78687,0.9887,2.06587,-1.27856,0.79225,-0.01164,2.48227,-0.07223})) 
	 * 9/10
	 * </pre>
	 */
	private static class Probability extends AbstractFunctionEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {

			if (ast.size() == 3) {
				try {
					if (ast.arg2().isList()) {
						IExpr predicate = ast.arg1();
						IAST data = (IAST) ast.arg2();
						if (predicate.isFunction()) {
							// Sum( Boole(predicate), data ) / data.argSize()
							int sum = 0;
							for (int i = 1; i < data.size(); i++) {
								if (engine.evalTrue(F.unaryAST1(predicate, data.get(i)))) {
									sum++;
								}
							}
							return F.QQ(sum, data.argSize());
						}
					} else if (ast.arg2().isAST(F.Distributed, 3)) {
						IExpr predicate = ast.arg1();
						IExpr x = ast.arg2().first();
						IExpr distribution = ast.arg2().second();
						if (distribution.isList()) {
							IAST data = (IAST) distribution;
							// Sum( Boole(predicate), data ) / data.argSize()
							int sum = 0;
							for (int i = 1; i < data.size(); i++) {
								if (engine.evalTrue(F.subst(predicate, F.Rule(x, data.get(i))))) {
									sum++;
								}
							}
							return F.QQ(sum, data.argSize());
						} else if (distribution.isDiscreteDistribution()) {
							IDiscreteDistribution dist = getDiscreteDistribution(distribution);
							int[] interval = dist.range(distribution, predicate, x);
							if (interval != null) {
								IExpr pdf = F.PDF.of(engine, distribution, x);
								// for discrete distributions take the sum:
								IASTAppendable sum = F.PlusAlloc(10);
								for (int i = interval[0]; i <= interval[1]; i++) {
									if (engine.evalTrue(F.subst(predicate, F.Rule(x, F.ZZ(i))))) {
										sum.append(F.subst(pdf, F.Rule(x, F.ZZ(i))));
									}
								}
								return sum;
							}
						}
					}
				} catch (Exception ex) {
					if (Config.SHOW_STACKTRACE) {
						ex.printStackTrace();
					}
				}
			}
			return F.NIL;
		}

	}

	/**
	 * <pre>
	 * PDF(distribution, value)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the probability density function of <code>value</code>.
	 * </p>
	 * </blockquote>
	 * 
	 * <pre>
	 * PDF(distribution, {list} )
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the probability density function of the values of list.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Probability_density_function">Wikipedia - probability density
	 * function</a></li>
	 * </ul>
	 * <p>
	 * <code>PDF</code> can be applied to the following distributions:
	 * </p>
	 * <blockquote>
	 * <p>
	 * <a href="BernoulliDistribution.md">BernoulliDistribution</a>,
	 * <a href="BinomialDistribution.md">BinomialDistribution</a>,
	 * <a href="DiscreteUniformDistribution.md">DiscreteUniformDistribution</a>,
	 * <a href="ErlangDistribution.md">ErlangDistribution</a>,
	 * <a href="ExponentialDistribution.md">ExponentialDistribution</a>,
	 * <a href="FrechetDistribution.md">FrechetDistribution</a>, <a href="GammaDistribution.md">GammaDistribution</a>,
	 * <a href="GeometricDistribution.md">GeometricDistribution</a>,
	 * <a href="GumbelDistribution.md">GumbelDistribution</a>,
	 * <a href="HypergeometricDistribution.md">HypergeometricDistribution</a>,
	 * <a href="LogNormalDistribution.md">LogNormalDistribution</a>,
	 * <a href="NakagamiDistribution.md">NakagamiDistribution</a>,
	 * <a href="NormalDistribution.md">NormalDistribution</a>, <a href="PoissonDistribution.md">PoissonDistribution</a>,
	 * <a href="StudentTDistribution.md">StudentTDistribution</a>,
	 * <a href="WeibullDistribution.md">WeibullDistribution</a>
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; PDF(NormalDistribution(n, m)) 
	 * 1/(Sqrt(2)*E^((-n+#1)^2/(2*m^2))*m*Sqrt(Pi))&amp;
	 * 
	 * &gt;&gt; PDF(GumbelDistribution(n, m),k)
	 * E^(-E^((k-n)/m)+(k-n)/m)/m
	 * 
	 * &gt;&gt; Table(PDF(NormalDistribution( ), x), {m, {-1, 1, 2}},{x, {-1, 1, 2}})//N  
	 * {{0.24197,0.24197,0.05399},{0.24197,0.24197,0.05399},{0.24197,0.24197,0.05399}}
	 * </pre>
	 */
	private static class PDF extends AbstractFunctionEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			// 1 or 2 arguments

			if (ast.size() == 2 || ast.size() == 3) {
				try {
					if (ast.arg1().isAST()) {
						IAST dist = (IAST) ast.arg1();
						IExpr xArg = F.NIL;
						if (ast.isAST2()) {
							xArg = ast.arg2();
						}
						if (dist.head().isSymbol()) {
							ISymbol head = (ISymbol) dist.head();

							if (dist.head().isSymbol()) {
								if (head instanceof IBuiltInSymbol) {
									IEvaluator evaluator = ((IBuiltInSymbol) head).getEvaluator();
									if (evaluator instanceof IPDF) {
										IPDF pdf = (IPDF) evaluator;
										return pdf.pdf(dist, xArg);
									}
								}
							}
						}
					}
				} catch (Exception ex) {
					if (Config.SHOW_STACKTRACE) {
						ex.printStackTrace();
					}
				}
			}
			return F.NIL;
		}

	}

	/**
	 * <pre>
	 * PoissonDistribution(m)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns a Poisson distribution.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:<br />
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Poisson_distribution">Wikipedia - Poisson distribution</a></li>
	 * </ul>
	 * <h3>Related terms</h3>
	 * <p>
	 * <a href="CDF.md">CDF</a>, <a href="Mean.md">Mean</a>, <a href="Mean.md">Median</a>, <a href="PDF.md">PDF</a>,
	 * <a href="Quantile.md">Quantile</a>, <a href="StandardDeviation.md">StandardDeviation</a>,
	 * <a href="Variance.md">Variance</a>
	 * </p>
	 */
	private final static class PoissonDistribution extends AbstractEvaluator
			implements ICDF, IDiscreteDistribution, IPDF, IVariance, IRandomVariate {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST1()) {
				return dist.arg1();
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr p = dist.arg1();
				//
				IExpr function =
						// [$ Piecewise({{GammaRegularized(1 + Floor(#), p), # >= 0}}, 0) & $]
						F.Function(F.Piecewise(F.List(F.List(F.GammaRegularized(F.Plus(F.C1, F.Floor(F.Slot1)), p),
								F.GreaterEqual(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr p = dist.arg1();
				//
				IExpr function =
						// [$ Piecewise({{p^#/(E ^ p * #!), # >= 0}}, 0) & $]
						F.Function(F.Piecewise(F.List(F.List(
								F.Times(F.Power(p, F.Slot1), F.Power(F.Times(F.Exp(p), F.Factorial(F.Slot1)), -1)),
								F.GreaterEqual(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST1()) {
				return dist.arg1();
			}
			return F.NIL;
		}

		public int getSupportUpperBound(IExpr discreteDistribution) {
			// probabilities are zero beyond that point
			return 1950;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

		@Override
		public IExpr randomVariate(Random random, IAST dist) {
			if (dist.isAST1()) {
				double mean = dist.arg1().evalDouble();
				return F.ZZ(new PoissonGenerator(mean, random).nextValue());
			}
			return F.NIL;
		}
	}

	/**
	 * <pre>
	 * Quantile(list, q)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns the <code>q</code>-Quantile of <code>list</code>.
	 * </p>
	 * </blockquote>
	 * 
	 * <pre>
	 * Quantile(list, {q1, q2, ...})
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns a list of the <code>q</code>-Quantiles of <code>list</code>.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Quantile">Wikipedia - Quantile</a></li>
	 * </ul>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Quantile({1,2}, 0.5)
	 * 1
	 * </pre>
	 */
	private final static class Quantile extends AbstractFunctionEvaluator implements QuantileRules {

		@Override
		public IAST getRuleAST() {
			return RULES;
		}

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkRange(ast, 2, 4);

			IExpr arg1 = ast.arg1();
			int[] dim = arg1.isMatrix();
			if (dim == null && arg1.isListOfLists()) {
				return F.NIL;
			}
			if (dim != null) {
				IAST matrix = (IAST) arg1;
				return matrix.mapMatrixColumns(dim, (IExpr x) -> ast.setAtClone(1, x));
			}

			if (arg1.isList()) {
				IExpr a = F.C0;
				IExpr b = F.C0;
				IExpr c = F.C1;
				IExpr d = F.C0;
				if (ast.size() == 4) {
					IExpr arg3 = ast.arg3();
					int[] dimParameters = arg3.isMatrix();
					if (dimParameters == null || dimParameters[0] != 2 || dimParameters[1] != 2) {
						return F.NIL;
					}
					a = arg3.first().first();
					b = arg3.first().second();
					c = arg3.second().first();
					d = arg3.second().second();
				}

				IAST list = (IAST) arg1;
				int dim1 = list.argSize();
				try {
					if (dim1 >= 0 && ast.size() >= 3) {

						final IAST s = EvalAttributes.copySortLess(list);
						final IInteger length = F.ZZ(s.argSize());

						IExpr q = ast.arg2();
						int dim2 = q.isVector();
						if (dim2 >= 0) {
							final IAST vector = ((IAST) q);
							if (vector.forAll(x -> x.isReal())) {
								return vector.map(scalar -> of(s, length, (ISignedNumber) scalar), 1);
							}
						} else {
							if (q.isReal()) {
								// x = a + (length + b) * q
								IExpr x = q.isZero() ? a : F.Plus.of(engine, a, F.Times(F.Plus(length, b), q));
								if (x.isNumIntValue()) {
									int index = x.toIntDefault(Integer.MIN_VALUE);
									if (index != Integer.MIN_VALUE) {
										if (index < 1) {
											index = 1;
										} else if (index > s.argSize()) {
											index = s.argSize();
										}
										return s.get(index);
									}
								}
								if (x.isReal()) {
									ISignedNumber xi = (ISignedNumber) x;
									int xFloor = xi.floorFraction().toIntDefault(Integer.MIN_VALUE);
									int xCeiling = xi.ceilFraction().toIntDefault(Integer.MIN_VALUE);
									if (xFloor != Integer.MIN_VALUE && xCeiling != Integer.MIN_VALUE) {
										if (xFloor < 1) {
											xFloor = 1;
										}
										if (xCeiling > s.argSize()) {
											xCeiling = s.argSize();
										}
										// factor = c + d * FractionalPart(x);
										IExpr factor = d.isZero() || xi.isZero() ? c
												: F.Plus.of(engine, c, F.Times(d, xi.fractionalPart()));
										// s[[Floor(x)]]+(s[[Ceiling(x)]]-s[[Floor(x)]]) * (c + d * FractionalPart(x))
										return F.Plus(s.get(xFloor), //
												F.Times(F.Subtract(s.get(xCeiling), s.get(xFloor)), factor));
									}
								}
								// return of(s, length, q);
							}
						}
					}
				} catch (ArithmeticException ae) {
					if (Config.SHOW_STACKTRACE) {
						ae.printStackTrace();
					}
				}
			} else if (arg1.isDistribution() && ast.size() >= 3) {
				IExpr function = engine.evaluate(F.Quantile(arg1));
				if (function.isFunction()) {
					if (ast.arg2().isList()) {
						return ((IAST) ast.arg2()).map(x -> F.unaryAST1(function, x), 1);
					}
					return F.unaryAST1(function, ast.arg2());
				}
			}
			return F.NIL;
		}

		private IExpr of(IAST sorted, IInteger length, ISignedNumber scalar) {
			if (scalar.isReal()) {
				int index = 0;
				if (scalar instanceof INum) {
					index = ((INum) scalar).multiply(length).ceilFraction().subtract(F.C1).toIntDefault(-1);
				} else {
					index = ((IRational) scalar).multiply(length).ceil().subtract(F.C1).toIntDefault(-1);
				}
				if (index >= 0) {
					return sorted.get(index + 1);
				}
			}
			throw new ArithmeticException();
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
			super.setUp(newSymbol);
		}
	}

	private final static class RandomVariate extends AbstractEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkRange(ast, 2, 3);

			if (ast.arg1().isAST()) {
				IAST dist = (IAST) ast.arg1();
				if (dist.head().isSymbol()) {
					try {
						ISymbol head = (ISymbol) dist.head();
						if (head instanceof IBuiltInSymbol) {
							IEvaluator evaluator = ((IBuiltInSymbol) head).getEvaluator();
							if (evaluator instanceof IRandomVariate) {
								Random random = ThreadLocalRandom.current();
								IRandomVariate variate = (IRandomVariate) evaluator;
								if (ast.size() == 3) {
									IExpr arg2 = ast.arg2();
									if (arg2.isList()) {
										int[] indx = Validate.checkListOfInts(arg2, 0, Integer.MAX_VALUE);
										IASTAppendable list = F.ListAlloc(indx[0]);
										return createArray(indx, 0, list, () -> variate.randomVariate(random, dist));
									} else {
										int n = arg2.toIntDefault(Integer.MIN_VALUE);
										if (n >= 0) {
											IASTAppendable result = F.ListAlloc(n);
											for (int i = 0; i < n; i++) {
												IExpr temp = variate.randomVariate(random, dist);
												if (temp.isPresent()) {
													result.append(variate.randomVariate(random, dist));
												} else {
													return F.NIL;
												}
											}
											return result;
										}
									}
									return F.NIL;
								}
								return variate.randomVariate(random, dist);
							}
						}
					} catch (RuntimeException ex) {
						engine.printMessage("RandomVariate: " + ex.getMessage());
						if (Config.SHOW_STACKTRACE) {
							ex.printStackTrace();
						}
					}
				}
			}

			return F.NIL;
		}

		private static IAST createArray(int[] indx, int offset, IASTAppendable list, Supplier<IExpr> s) {
			if (indx.length <= offset) {
				list.append(s.get());
				return list;
			}
			IASTAppendable subList = F.ListAlloc(indx[offset]);
			for (int i = 1; i <= indx[offset]; i++) {
				createArray(indx, offset + 1, subList, s);
			}
			list.append(subList);
			return subList;
		}
	}

	/**
	 * <pre>
	 * Rescale(list)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns <code>Rescale(list,{Min(list), Max(list)})</code>.
	 * </p>
	 * </blockquote>
	 * 
	 * <pre>
	 * Rescale(x,{xmin, xmax})
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns <code>x/(xmax-xmin)-xmin/(xmax-xmin)</code>.
	 * </p>
	 * </blockquote>
	 * 
	 * <pre>
	 * Rescale(x,{xmin, xmax},{ymin, ymax})
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns <code>(x*(ymax-ymin))/(xmax-xmin)-(xmin*ymax-xmax*ymin)/(xmax-xmin)</code>.
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Rescale({a,b})
	 * {a/(Max(a,b)-Min(a,b))-Min(a,b)/(Max(a,b)-Min(a,b)),b/(Max(a,b)-Min(a,b))-Min(a,b)/(Max(a,b)-Min(a,b))}
	 * 
	 * &gt;&gt; Rescale({1, 2, 3, 4, 5}, {-100, 100})
	 * {101/200,51/100,103/200,13/25,21/40}
	 * 
	 * &gt;&gt; Rescale(x,{xmin, xmax},{ymin, ymax})
	 * (x*(ymax-ymin))/(xmax-xmin)-(xmin*ymax-xmax*ymin)/(xmax-xmin)
	 * </pre>
	 */
	private final static class Rescale extends AbstractEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkRange(ast, 2, 4);
			IExpr x = ast.arg1();
			if (ast.size() == 2 && x.isList()) {
				IExpr min = F.Min.of(engine, x);
				IExpr max = F.Max.of(engine, x);
				return rescale(x, min, max, engine);
			}
			if (ast.size() >= 3) {
				if (ast.arg2().isAST(F.List, 3)) {
					IAST list1 = (IAST) ast.arg2();
					IExpr min = list1.first();
					IExpr max = list1.second();
					if (ast.size() == 4) {
						if (ast.arg3().isAST(F.List, 3)) {
							IAST list2 = (IAST) ast.arg3();
							IExpr ymin = list2.first();
							IExpr ymax = list2.second();
							// (arg1*(ymax - ymin))/(max - min) - (min*ymax - max*ymin)/(max - min)
							return engine.evaluate(F.Plus(
									F.Times(x, F.Power(F.Plus(max, F.Negate(min)), -1), F.Plus(ymax, F.Negate(ymin))),
									F.Times(F.CN1, F.Power(F.Plus(max, F.Negate(min)), -1),
											F.Plus(F.Times(min, ymax), F.Times(F.CN1, max, ymin)))));
						}
						return F.NIL;
					}
					return rescale(x, min, max, engine);
				}
				return F.NIL;
			}

			return F.NIL;
		}

		private static IExpr rescale(IExpr x, IExpr min, IExpr max, EvalEngine engine) {
			IExpr sum = engine.evaluate(F.Subtract(max, min));
			return engine.evaluate(F.Plus(F.Times(F.CN1, F.Power(sum, -1), min), F.Times(F.Power(sum, -1), x)));
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	/**
	 * <pre>
	 * Skewness(list)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * gives Pearson's moment coefficient of skewness for $list$ (a measure for estimating the symmetry of a
	 * distribution).
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt;&gt; Skewness({1.1, 1.2, 1.4, 2.1, 2.4})
	 * 0.40704
	 * </pre>
	 */
	private final static class Skewness extends AbstractEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			if (ast.arg1().isList()) {
				IAST list = (IAST) ast.arg1();
				return F.Divide(F.CentralMoment(list, F.C3), F.Power(F.CentralMoment(list, F.C2), F.C3D2));
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	/**
	 * <pre>
	 * StandardDeviation(list)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * computes the standard deviation of <code>list</code>. <code>list</code> may consist of numerical values or
	 * symbols. Numerical values may be real or complex.
	 * </p>
	 * </blockquote>
	 * <p>
	 * <code>StandardDeviation({{a1, a2, ...}, {b1, b2, ...}, ...})</code> will yield
	 * <code>{StandardDeviation({a1, b1, ...}, StandardDeviation({a2, b2, ...}), ...}</code>.
	 * </p>
	 * <p>
	 * <code>StandardDeviation</code> can be applied to the following distributions:
	 * </p>
	 * <blockquote>
	 * <p>
	 * <a href="BernoulliDistribution.md">BernoulliDistribution</a>,
	 * <a href="BinomialDistribution.md">BinomialDistribution</a>,
	 * <a href="DiscreteUniformDistribution.md">DiscreteUniformDistribution</a>,
	 * <a href="ErlangDistribution.md">ErlangDistribution</a>,
	 * <a href="ExponentialDistribution.md">ExponentialDistribution</a>,
	 * <a href="FrechetDistribution.md">FrechetDistribution</a>, <a href="GammaDistribution.md">GammaDistribution</a>,
	 * <a href="GeometricDistribution.md">GeometricDistribution</a>,
	 * <a href="GumbelDistribution.md">GumbelDistribution</a>,
	 * <a href="HypergeometricDistribution.md">HypergeometricDistribution</a>,
	 * <a href="LogNormalDistribution.md">LogNormalDistribution</a>,
	 * <a href="NakagamiDistribution.md">NakagamiDistribution</a>,
	 * <a href="NormalDistribution.md">NormalDistribution</a>, <a href="PoissonDistribution.md">PoissonDistribution</a>,
	 * <a href="StudentTDistribution.md">StudentTDistribution</a>,
	 * <a href="WeibullDistribution.md">WeibullDistribution</a>
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; StandardDeviation({1, 2, 3})
	 * 1
	 * 
	 * &gt;&gt; StandardDeviation({7, -5, 101, 100})
	 * Sqrt(13297)/2
	 * 
	 * &gt;&gt; StandardDeviation({a, a})  
	 * 0
	 * 
	 * &gt;&gt; StandardDeviation({{1, 10}, {-1, 20}})
	 * {Sqrt(2),5*Sqrt(2)}
	 * 
	 * &gt;&gt; StandardDeviation(LogNormalDistribution(0, 1))
	 * Sqrt((-1+E)*E)
	 * </pre>
	 */
	private final static class StandardDeviation extends AbstractFunctionEvaluator implements StandardDeviationRules {

		@Override
		public IAST getRuleAST() {
			return RULES;
		}

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);

			if (ast.arg1().isList()) {
				IAST arg1 = (IAST) ast.arg1();
				int[] dim = arg1.isMatrix();
				if (dim == null && arg1.isListOfLists()) {
					return F.NIL;
				}
				if (dim != null) {
					IAST matrix = arg1;
					return matrix.mapMatrixColumns(dim, x -> F.StandardDeviation(x));
				}
			}
			return F.Sqrt(F.Variance(ast.arg1()));
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
			super.setUp(newSymbol);
		}
	}

	private final static class Standardize extends AbstractEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			IExpr arg1 = ast.arg1();

			int[] dim = arg1.isMatrix();
			if (dim == null && arg1.isListOfLists()) {
				return F.NIL;
			}
			if (dim != null) {
				IAST matrix = (IAST) arg1;
				return F.Transpose(matrix.mapMatrixColumns(dim, v -> F.Standardize(v)));
			}

			IExpr sd = F.StandardDeviation.of(engine, arg1);
			if (!sd.isZero()) {
				return engine.evaluate(F.Divide(F.Subtract(arg1, F.Mean(arg1)), sd));
			}
			return F.NIL;
		}

	}

	private final static class StudentTDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			// 1 or 3 args
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST1()) {
				// (v) -> Piecewise({{0, v > 1}}, Indeterminate)
				return F.Piecewise(F.List(F.List(F.C0, F.Greater(dist.arg1(), F.C1))), F.Indeterminate);
			}
			if (dist.isAST3()) {
				// (m,s,v) -> Piecewise({{m, v > 1}}, Indeterminate)
				return F.Piecewise(F.List(F.List(dist.arg1(), F.Greater(dist.arg3(), F.C1))), F.Indeterminate);
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST1()) {
				return F.C0;
			}

			if (dist.isAST3()) {
				// (m,s,v) -> m
				return dist.arg1();
			}

			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr n = dist.arg1();
				//
				IExpr function =
						// [$ Piecewise({{(1/2)*BetaRegularized(n/(#^2 + n), n/2, 1/2), # <= 0}}, (1/2)*(1 +
						// BetaRegularized(#^2/(#^2 + n), 1/2, n/2))) & $]
						F.Function(F.Piecewise(
								F.List(F.List(F.Times(F.C1D2,
										F.BetaRegularized(F.Times(n, F.Power(F.Plus(F.Sqr(F.Slot1), n), -1)),
												F.Times(F.C1D2, n), F.C1D2)),
										F.LessEqual(F.Slot1, F.C0))),
								F.Times(F.C1D2,
										F.Plus(F.C1,
												F.BetaRegularized(
														F.Times(F.Power(F.Plus(F.Sqr(F.Slot1), n), -1), F.Sqr(F.Slot1)),
														F.C1D2, F.Times(F.C1D2, n)))))); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST1()) {
				IExpr n = dist.arg1();
				//
				IExpr function =
						// [$ (n/(#^2 + n))^((1 + n)/2)/(Sqrt(n)*Beta(n/2, 1/2)) & $]
						F.Function(F.Times(
								F.Power(F.Times(n, F.Power(F.Plus(F.Sqr(F.Slot1), n), -1)),
										F.Times(F.C1D2, F.Plus(F.C1, n))),
								F.Power(F.Times(F.Sqrt(n), F.Beta(F.Times(F.C1D2, n), F.C1D2)), -1))); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST1()) {
				IExpr n = dist.arg1();
				return F.Piecewise(F.List(F.List(F.Divide(n, F.Plus(F.CN2, n)), F.Greater(n, F.C2))), F.Indeterminate);
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}

	}

	private final static class UniformDistribution extends AbstractEvaluator
			implements IDistribution, IVariance, ICDF, IPDF, IRandomVariate {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				// (max + min)/2
				return F.Times(F.C1D2, F.Plus(minMax[0], minMax[1]));
			}
			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				IExpr l = minMax[0];
				IExpr r = minMax[1];
				return
				// [$ (l + r)/2 $]
				F.Times(F.C1D2, F.Plus(l, r)); // $$;
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				IExpr l = minMax[0];
				IExpr r = minMax[1];
				return
				// [$ (1/12)*(l - r)^2 $]
				F.Times(F.QQ(1L, 12L), F.Sqr(F.Plus(l, F.Negate(r)))); // $$;
			}

			return F.NIL;
		}

		public IExpr[] minmax(IAST dist) {
			if (dist.size() == 2 && dist.arg1().isList()) {
				IAST list = (IAST) dist.arg1();
				if (list.isAST2()) {
					IExpr l = list.arg1();
					IExpr r = list.arg2();
					return new IExpr[] { l, r };
				}
			} else if (dist.size() == 1) {
				return new IExpr[] { F.C0, F.C1 };
			}
			return null;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				IExpr a = minMax[0];
				IExpr b = minMax[1];
				IExpr function =
						// [$ Piecewise({{(# - a)/(b - a), a <= # <= b}, {1, # > b}}, 0) & $]
						F.Function(
								F.Piecewise(F.List(
										F.List(F.Times(F.Power(F.Plus(F.Negate(a), b), -1),
												F.Plus(F.Negate(a), F.Slot1)), F.LessEqual(a, F.Slot1, b)),
										F.List(F.C1, F.Greater(F.Slot1, b))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				IExpr a = minMax[0];
				IExpr b = minMax[1];
				IExpr function =
						// [$ Piecewise({{1/(b - a), a <= # <= b}}, 0)& $]
						F.Function(F.Piecewise(
								F.List(F.List(F.Power(F.Plus(F.Negate(a), b), -1), F.LessEqual(a, F.Slot1, b))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr randomVariate(Random random, IAST dist) {
			IExpr[] minMax = minmax(dist);
			if (minMax != null) {
				ISignedNumber min = minMax[0].evalReal();
				ISignedNumber max = minMax[1].evalReal();
				if (min != null && max != null) {
					RandomDataGenerator rdg = new RandomDataGenerator();
					return F.num(rdg.nextUniform(min.doubleValue(), max.doubleValue()));
				}
			}
			return F.NIL;
		}
	}

	/**
	 * <pre>
	 * Variance(list)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * computes the variance of <code>list</code>. <code>list</code> may consist of numerical values or symbols.
	 * Numerical values may be real or complex.
	 * </p>
	 * </blockquote>
	 * <p>
	 * <code>Variance({{a1, a2, ...}, {b1, b2, ...}, ...})</code> will yield
	 * <code>{Variance({a1, b1, ...}, Variance({a2, b2, ...}), ...}</code>.
	 * </p>
	 * <p>
	 * <code>Variance</code> can be applied to the following distributions:
	 * </p>
	 * <blockquote>
	 * <p>
	 * <a href="BernoulliDistribution.md">BernoulliDistribution</a>,
	 * <a href="BinomialDistribution.md">BinomialDistribution</a>,
	 * <a href="DiscreteUniformDistribution.md">DiscreteUniformDistribution</a>,
	 * <a href="ErlangDistribution.md">ErlangDistribution</a>,
	 * <a href="ExponentialDistribution.md">ExponentialDistribution</a>,
	 * <a href="FrechetDistribution.md">FrechetDistribution</a>, <a href="GammaDistribution.md">GammaDistribution</a>,
	 * <a href="GeometricDistribution.md">GeometricDistribution</a>,
	 * <a href="GumbelDistribution.md">GumbelDistribution</a>,
	 * <a href="HypergeometricDistribution.md">HypergeometricDistribution</a>,
	 * <a href="LogNormalDistribution.md">LogNormalDistribution</a>,
	 * <a href="NakagamiDistribution.md">NakagamiDistribution</a>,
	 * <a href="NormalDistribution.md">NormalDistribution</a>, <a href="PoissonDistribution.md">PoissonDistribution</a>,
	 * <a href="StudentTDistribution.md">StudentTDistribution</a>,
	 * <a href="WeibullDistribution.md">WeibullDistribution</a>
	 * </p>
	 * </blockquote>
	 * <h3>Examples</h3>
	 * 
	 * <pre>
	 * &gt;&gt; Variance({1, 2, 3})
	 * 1
	 * 
	 * &gt;&gt; Variance({7, -5, 101, 3})
	 * 7475/3
	 * 
	 * &gt;&gt; Variance({1 + 2*I, 3 - 10*I})
	 * 74
	 * 
	 * &gt;&gt; Variance({a, a})
	 * 0
	 * 
	 * &gt;&gt; Variance({{1, 3, 5}, {4, 10, 100}})
	 * {9/2,49/2,9025/2}
	 * </pre>
	 */
	private final static class Variance extends AbstractFunctionEvaluator {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			Validate.checkSize(ast, 2);

			if (ast.arg1().isAST()) {
				try {
					IAST arg1 = (IAST) ast.arg1();
					int[] matrixDimensions = arg1.isMatrix();
					if (matrixDimensions != null) {
						if (arg1.isRealMatrix()) {
							double[][] matrix = arg1.toDoubleMatrix();
							matrix = Convert.toDoubleTransposed(matrix);
							double[] result = new double[matrixDimensions[1]];
							for (int i = 0; i < matrix.length; i++) {
								result[i] = StatUtils.variance(matrix[i]);
							}
							return new ASTRealVector(result, false);
						}
						IASTAppendable result = F.ListAlloc(matrixDimensions[0]);
						for (int i = 1; i < matrixDimensions[1] + 1; i++) {
							final int ii = i;
							IASTAppendable list = F.ListAlloc(matrixDimensions[1]);
							IAST variance = F.Variance(list);
							list.appendArgs(matrixDimensions[0] + 1, j -> arg1.getPart(j, ii));
							result.append(variance);
						}
						return result;
					}

					int dim = arg1.isVector();
					if (dim >= 0) {
						if (arg1.isRealVector()) {
							return F.num(StatUtils.variance(arg1.toDoubleVector()));
						}
						return Covariance.vectorCovarianceSymbolic(arg1, arg1, dim);
					}

					if (arg1.isAST()) {
						IAST dist = arg1;
						if (dist.head().isSymbol()) {
							ISymbol head = (ISymbol) dist.head();
							if (head instanceof IBuiltInSymbol) {
								IEvaluator evaluator = ((IBuiltInSymbol) head).getEvaluator();
								if (evaluator instanceof IVariance) {
									IVariance distribution = (IVariance) evaluator;
									return distribution.variance(dist);
								}
							}
						}
					}
				} catch (Exception ex) {
					if (Config.SHOW_STACKTRACE) {
						ex.printStackTrace();
					}
				}
			}
			return F.NIL;
		}

	}

	/**
	 * <pre>
	 * WeibullDistribution(a, b)
	 * </pre>
	 * 
	 * <blockquote>
	 * <p>
	 * returns a Weibull distribution.
	 * </p>
	 * </blockquote>
	 * <p>
	 * See:<br />
	 * </p>
	 * <ul>
	 * <li><a href="https://en.wikipedia.org/wiki/Weibull_distribution">Wikipedia - Weibull distribution</a></li>
	 * </ul>
	 * <h3>Related terms</h3>
	 * <p>
	 * <a href="CDF.md">CDF</a>, <a href="Mean.md">Mean</a>, <a href="Mean.md">Median</a>, <a href="PDF.md">PDF</a>,
	 * <a href="Quantile.md">Quantile</a>, <a href="StandardDeviation.md">StandardDeviation</a>,
	 * <a href="Variance.md">Variance</a>
	 * </p>
	 */
	private final static class WeibullDistribution extends AbstractEvaluator
			implements ICDF, IDistribution, IPDF, IVariance {

		@Override
		public IExpr evaluate(final IAST ast, EvalEngine engine) {
			// 2 or 3 args
			return F.NIL;
		}

		@Override
		public IExpr mean(IAST dist) {
			if (dist.isAST2()) {
				// (a,b) -> b*Gamma(1 + 1/a)
				return F.Times(dist.arg2(), F.Gamma(F.Plus(F.C1, F.Power(dist.arg1(), F.CN1))));
			}
			if (dist.isAST3()) {
				// (a,b,m) -> m + b*Gamma(1 + 1/a)
				return F.Plus(dist.arg3(), F.Times(dist.arg2(), F.Gamma(F.Plus(F.C1, F.Power(dist.arg1(), F.CN1)))));
			}

			return F.NIL;
		}

		@Override
		public IExpr median(IAST dist) {
			if (dist.isAST2()) {
				// (a,b) -> b*Log(2)^(1/a)
				IExpr a = dist.arg1();
				IExpr b = dist.arg2();
				return F.Times(b, F.Power(F.Log(F.C2), F.Power(a, -1)));
			}

			if (dist.isAST3()) {
				// (a,b,m) -> m + b*Log(2)^(1/a)
				IExpr a = dist.arg1();
				IExpr b = dist.arg2();
				IExpr m = dist.arg3();
				return F.Plus(m, F.Times(b, F.Power(F.Log(F.C2), F.Power(a, -1))));
			}

			return F.NIL;
		}

		@Override
		public IExpr cdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				//
				IExpr function =
						// [$ Piecewise({{1 - E^(-(#/m)^n),# > 0}}, 0) & $]
						F.Function(
								F.Piecewise(F.List(F.List(
										F.Plus(F.C1,
												F.Negate(
														F.Exp(F.Negate(F.Power(F.Times(F.Power(m, -1), F.Slot1), n))))),
										F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr pdf(IAST dist, IExpr k) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				//
				IExpr function =
						// [$ Piecewise({{((#/m)^(-1 + n)*n)/(E^(#/m)^n*m), # > 0}}, 0) & $]
						F.Function(F.Piecewise(F.List(F.List(
								F.Times(F.Power(F.Times(F.Exp(F.Power(F.Times(F.Power(m, -1), F.Slot1), n)), m), -1), n,
										F.Power(F.Times(F.Power(m, -1), F.Slot1), F.Plus(F.CN1, n))),
								F.Greater(F.Slot1, F.C0))), F.C0)); // $$;
				return callFunction(function, k);
			}
			return F.NIL;
		}

		@Override
		public IExpr variance(IAST dist) {
			if (dist.isAST2()) {
				IExpr n = dist.arg1();
				IExpr m = dist.arg2();
				// m^2*(-Gamma(1 + 1/n)^2 + Gamma(1 + 2/n))
				return F.Times(F.Sqr(m), F.Plus(F.Negate(F.Sqr(F.Gamma(F.Plus(F.C1, F.Power(n, -1))))),
						F.Gamma(F.Plus(F.C1, F.Times(F.C2, F.Power(n, -1))))));
			}
			return F.NIL;
		}

		@Override
		public void setUp(final ISymbol newSymbol) {
		}
	}

	private final static StatisticsFunctions CONST = new StatisticsFunctions();

	public static StatisticsFunctions initialize() {
		return CONST;
	}

	private StatisticsFunctions() {

	}

}
