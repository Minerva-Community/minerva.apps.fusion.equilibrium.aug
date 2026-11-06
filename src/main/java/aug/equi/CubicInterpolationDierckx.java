package aug.equi;

// DISABLED until dierckx-java is fixed and deployed to maven.minerva.cloud (see the issue at
// https://github.com/Minerva-Community/dierckx-java/issues).
// The superclass DierckxGridInterpolation2D now lives in dierckx-java, in the same package
// (algorithmrepository.dierckx). AlgorithmRepository has only an empty final stub of it, so this
// does not compile without dierckx-java. See AUGEquilibriumData.DIERCKX_UNAVAILABLE.
// To restore: uncomment below, add net.minerva-central:dierckx-java to pom.xml BEFORE
// AlgorithmRepository, and use it again in AUGEquilibriumData.createPsiInterpolator().
//
//import algorithmrepository.dierckx.DierckxGridInterpolation2D;
//
///**
// * TODO: move to dierckx-java
// */
//public class CubicInterpolationDierckx extends DierckxGridInterpolation2D implements algorithmrepository.interfaces.Interpolation2D  {
//
//
//	public CubicInterpolationDierckx(double[] x, double[] y, double[][] z,
//			double[] bbox, int kx, int ky, double s) {
//		super(x, y, z, bbox, kx, ky, s);
//	}
//
//	public CubicInterpolationDierckx(double[] x, double[] y, double[][] z) {
//		super(x, y, z);
//	}
//
//	@Override
//	public double[] getX() { return xp; }
//
//	@Override
//	public double[] getY() { return yp; }
//
//	@Override
//	public double[][] getF() { return zp; }
//
//	@Override
//	public void setF(double[][] fp) {
//		zp = fp;
//		init();
//	}
//
//	@Override
//	public double eval(double x, double y) {
//		return evalPoints(new double[]{ x }, new double[]{ y }, null)[0];
//	}
//
//	@Override
//	public double eval(double x, double y, double[] der) {
//		double derIntern[][] = new double[der.length][1];
//		double val = evalPoints(new double[]{ x }, new double[]{ y }, derIntern)[0];
//		for(int iD=0; iD < der.length; iD++)
//			der[iD] = derIntern[iD][0];
//		return val;
//	}
//
//	@Override
//	public double[] eval(double[] x, double[] y, double[][] der) {
//		double derIntern[][] = new double[der[0].length][der.length];
//		double vals[] = evalPoints(x, y, derIntern);
//		for(int iP=0; iP < x.length; iP++){
//			for(int iD=0; iD < der[0].length; iD++){
//				der[iP][iD] = derIntern[iD][iP];
//			}
//		}
//		return vals;
//	}
//
//	@Override
//	public double[] eval(double[] x, double[] y) {
//		return evalPoints(x, y, null);
//	}
//
//
//}
