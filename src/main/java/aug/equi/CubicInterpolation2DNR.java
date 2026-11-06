package aug.equi;

import algorithmrepository.interfaces.Interpolation2D;


/**
 * TODO: move to AlgorithmRepository ???
 *
 * Slow, based on NR 1D cubic interp
 */
public class CubicInterpolation2DNR implements Interpolation2D {
    private double[] xp, yp;
    private double[][] fp;
    private double fSp[][]; //spline coefficients for each row

    public CubicInterpolation2DNR(double[] xp, double[] yp, double[][] fp) {
       this.xp = xp;
       this.yp = yp;
       this.fp = fp;
       init();
    }

    private void init(){
       fSp = new double[yp.length][];
       for(int iX=0; iX < xp.length; iX++){
    	   fSp[iX] = Splining.spline(yp, fp[iX], 0, 0);
       }
    }

    @Override
    public double eval(double x, double y) {
    	return 0;
    }

	@Override
	public double[] getX() { return xp; }

	@Override
	public double[] getY() { return yp; }

	@Override
	public double[][] getF() { return fp; }

	@Override
	public void setF(double[][] fp) {
		this.fp = fp;
		init();
	}

	@Override
	public double[] eval(double[] x, double[] y, double[][] der) {
		double ret[] = new double[x.length];
		double fRow[] = new double[xp.length];
		double derRow[] = new double[xp.length];
		double fRowSp[] = null;
		double derRowSp[] = null;
		double tmp[] = new double[1];

		for(int i = 0; i < x.length; i++){
			if(i == 0 || y[i] != y[i-1]){
				for(int iX=0; iX < xp.length; iX++){
					fRow[iX] = Splining.splint(yp, fp[iX], fSp[iX], y[i], tmp);
					derRow[iX] = tmp[0]; //dfdy at eval y on column iX
				}
				fRowSp = Splining.spline(xp, fRow, 0, 0);
				derRowSp = Splining.spline(xp, derRow, 0, 0);
			}
			ret[i] = Splining.splint(xp, fRow, fRowSp, x[i], tmp);
			der[i][0] = tmp[0]; // dfdx
			der[i][1] = Splining.splint(xp, derRow, derRowSp, x[i], tmp); //dfdy, interpolated
			der[i][2] = tmp[0]; //d2fdxdy
		}
		return ret;
	}

	@Override
	public double eval(double x, double y, double[] der) {

		double fRow[] = new double[xp.length];
		double derRow[] = new double[xp.length];
		double fRowSp[] = null;
		double derRowSp[] = null;
		double tmp[] = new double[1];

		for(int iX=0; iX < xp.length; iX++){
			fRow[iX] = Splining.splint(yp, fp[iX], fSp[iX], y, tmp);
			derRow[iX] = tmp[0]; //dfdy at eval y on column iX
		}
		fRowSp = Splining.spline(xp, fRow, 0, 0);
		derRowSp = Splining.spline(xp, derRow, 0, 0);

		double ret = Splining.splint(xp, fRow, fRowSp, x, tmp);
		der[0] = tmp[0]; // dfdx
		der[1] = Splining.splint(xp, derRow, derRowSp, x, tmp); //dfdy, interpolated
		der[2] = tmp[0]; //d2fdxdy

		return ret;
	}

	@Override
	public double[] eval(double[] x, double[] y) {
		double ret[] = new double[x.length];
		double fRow[] = new double[xp.length];
		double fRowSp[] = null;
		double tmp[] = new double[1];

		for(int i = 0; i < x.length; i++){
			if(i == 0 || y[i] != y[i-1]){
				for(int iX=0; iX < xp.length; iX++){
					fRow[iX] = Splining.splint(yp, fp[iX], fSp[iX], y[i], tmp);
				}
				fRowSp = Splining.spline(xp, fRow, 0, 0);
			}
			ret[i] = Splining.splint(xp, fRow, fRowSp, x[i], null);
		}
		return ret;
	}
}
