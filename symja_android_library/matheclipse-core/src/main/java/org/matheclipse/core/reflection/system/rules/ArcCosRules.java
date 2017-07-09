package org.matheclipse.core.reflection.system.rules;

import static org.matheclipse.core.expression.F.*;
import org.matheclipse.core.interfaces.IAST;

/**
 * <p>Generated by <code>org.matheclipse.core.preprocessor.RulePreprocessor</code>.</p>
 * <p>See GIT repository at: <a href="https://bitbucket.org/axelclk/symja_android_library">bitbucket.org/axelclk/symja_android_library under the tools directory</a>.</p>
 */
public interface ArcCosRules {
  /**
   * <ul>
   * <li>index 0 - number of equal rules in <code>RULES</code></li>
	 * </ul>
	 */
  final public static int[] SIZES = { 20, 0 };

  final public static IAST RULES = List(
    IInit(ArcCos, SIZES),
    // ArcCos(0)=Rational(1,2)*Pi
    ISet(ArcCos(C0),
      Times(C1D2,Pi)),
    // ArcCos(Rational(1,2))=Rational(1,3)*Pi
    ISet(ArcCos(C1D2),
      Times(C1D3,Pi)),
    // ArcCos(Rational(-1,2))=Rational(2,3)*Pi
    ISet(ArcCos(CN1D2),
      Times(QQ(2L,3L),Pi)),
    // ArcCos(Rational(1,2)*Sqrt(2))=Rational(1,4)*Pi
    ISet(ArcCos(C1DSqrt2),
      Times(C1D4,Pi)),
    // ArcCos(Rational(-1,2)*Sqrt(2))=Rational(3,4)*Pi
    ISet(ArcCos(Negate(C1DSqrt2)),
      Times(QQ(3L,4L),Pi)),
    // ArcCos(Rational(1,2)*Sqrt(3))=Rational(1,6)*Pi
    ISet(ArcCos(Times(C1D2,CSqrt3)),
      Times(QQ(1L,6L),Pi)),
    // ArcCos(Rational(-1,2)*Sqrt(3))=Rational(5,6)*Pi
    ISet(ArcCos(Times(CN1D2,CSqrt3)),
      Times(QQ(5L,6L),Pi)),
    // ArcCos(Rational(1,2)*Sqrt(2+Sqrt(2)))=Rational(1,8)*Pi
    ISet(ArcCos(Times(C1D2,Sqrt(Plus(C2,CSqrt2)))),
      Times(QQ(1L,8L),Pi)),
    // ArcCos(Rational(-1,2)*Sqrt(2+Sqrt(2)))=Rational(7,8)*Pi
    ISet(ArcCos(Times(CN1D2,Sqrt(Plus(C2,CSqrt2)))),
      Times(QQ(7L,8L),Pi)),
    // ArcCos((1+Sqrt(3))/(2*Sqrt(2)))=Rational(1,12)*Pi
    ISet(ArcCos(Times(C1D2,C1DSqrt2,Plus(C1,CSqrt3))),
      Times(QQ(1L,12L),Pi)),
    // ArcCos((-1-Sqrt(3))/(2*Sqrt(2)))=Rational(11,12)*Pi
    ISet(ArcCos(Times(C1D2,C1DSqrt2,Plus(CN1,Negate(CSqrt3)))),
      Times(QQ(11L,12L),Pi)),
    // ArcCos(1)=0
    ISet(ArcCos(C1),
      C0),
    // ArcCos(-1)=Pi
    ISet(ArcCos(CN1),
      Pi),
    // ArcCos(I)=Rational(1,2)*Pi+I*Log(Sqrt(2)+(-1)*1)
    ISet(ArcCos(CI),
      Plus(Times(C1D2,Pi),Times(CI,Log(Plus(CN1,CSqrt2))))),
    // ArcCos((-1)*I)=Rational(1,2)*Pi+I*Log(1+Sqrt(2))
    ISet(ArcCos(CNI),
      Plus(Times(C1D2,Pi),Times(CI,Log(Plus(C1,CSqrt2))))),
    // ArcCos(Infinity)=I*Infinity
    ISet(ArcCos(oo),
      DirectedInfinity(CI)),
    // ArcCos(-Infinity)=(-1)*I*Infinity
    ISet(ArcCos(Noo),
      DirectedInfinity(CNI)),
    // ArcCos(I*Infinity)=(-1)*I*Infinity
    ISet(ArcCos(DirectedInfinity(CI)),
      DirectedInfinity(CNI)),
    // ArcCos((-1)*I*Infinity)=I*Infinity
    ISet(ArcCos(DirectedInfinity(CNI)),
      DirectedInfinity(CI)),
    // ArcCos(ComplexInfinity)=ComplexInfinity
    ISet(ArcCos(CComplexInfinity),
      CComplexInfinity)
  );
}
