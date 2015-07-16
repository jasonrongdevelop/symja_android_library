package org.matheclipse.core.interfaces;


/**
 * Interface for pattern objects (i.e. x_)
 * 
 */
public interface IPattern extends IPatternObject, IExpr {

	/**
	 * Return <code>true</code>, if the expression fullfills the patterns
	 * additional condition
	 * 
	 * @param expr
	 * @return
	 */
	public boolean isConditionMatched(IExpr expr);

}
