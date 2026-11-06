package aug.equi;

import algorithmrepository.Interpolation2D;
import algorithmrepository.InterpolationMode;

/**
 * Bicubic interpolation through the old {@link algorithmrepository.interfaces.Interpolation2D}
 * interface, which the AUG equilibrium code still returns.
 *
 * The old algorithmrepository.CubicInterpolation2D was removed from AlgorithmRepository (it threw
 * from 1.4.11, and was emptied in 1.4.27). Its successor is {@link Interpolation2D} with
 * {@link InterpolationMode#CUBIC}, which uses the same bicubic algorithm but no longer implements
 * the old interface. This class wraps it.
 *
 * Both return NaN outside the grid. Derivative arrays are [nPoints][2 or 3] = dfdx, dfdy, (d2fdxdy),
 * the same as the old class.
 */
@SuppressWarnings("deprecation")
public class CubicInterpolation2DAdapter implements algorithmrepository.interfaces.Interpolation2D {

	private final Interpolation2D interp;

	public CubicInterpolation2DAdapter(double[] xp, double[] yp, double[][] fp) {
		interp = new Interpolation2D(xp, yp, fp, InterpolationMode.CUBIC);
	}

	@Override
	public double[] getX() { return interp.getX(); }

	@Override
	public double[] getY() { return interp.getY(); }

	@Override
	public double[][] getF() { return interp.getF(); }

	@Override
	public void setF(double[][] fp) { interp.setF(fp); }

	@Override
	public double eval(double x, double y) {
		return interp.eval(x, y);
	}

	@Override
	public double eval(double x, double y, double[] der) {
		return interp.eval(x, y, der);
	}

	@Override
	public double[] eval(double[] x, double[] y) {
		return interp.eval(new double[][] { x, y }, null);
	}

	@Override
	public double[] eval(double[] x, double[] y, double[][] der) {
		return interp.eval(new double[][] { x, y }, der);
	}
}
