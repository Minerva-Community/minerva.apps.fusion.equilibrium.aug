package aug.equi;

import mds.AugShotfileFetcher;
import descriptors.aug.AUGSignalDesc;
import algorithmrepository.Algorithms;
import algorithmrepository.ExtrapolationMode;
import algorithmrepository.Interpolation1D;
import algorithmrepository.InterpolationMode;
import algorithmrepository.Mat;
import algorithmrepository.interfaces.Interpolation2D;
import oneLiners.OneLiners;
import signals.aug.AUGSignal;

/**
 * Simplified access to flux from the weirdly stored AUG CLISTE-style shot files
 *
 * Shouldn't really be in DataSignals, but I don't quite know what to do with it
 *
 * @author oliford
 *
 */
public class AUGEquilibriumData {
	private static final double mu0 = 1.25663706e-6;

	private String bvacSignalPath = "sig/MAI/BTF"; //toroidal magnetic field at R=1.65 m
	private double bvacMeasurementLocation = 1.65; // R / m

	private String diag = null;
	private String exp = null;
	private int pulse = -1;

	private double[] dataIp;
	private double[] dataBvac;
	private double[] dataBvacTime;
	private double[][] dataR;
	private double[][] dataZ;
	private double[] dataTimeOrdered;
	private double[][][] dataPsi;
	private double[][] dataJPol;
	private double[][] dataPsiLabel;

	/** magAxis, Separatrix, Limiter, ...?*/
	private double[][] dataPsiNorms;
	private int[] indexInUnordered;

	public void loadData(int pulse){
		loadData("AUGD", "EQI", pulse);
	}

	public void loadData(String exp, String diag, int pulse){
		this.pulse = -1;
		this.diag = null;
		this.exp = null;
		dataPsi = null; dataR = null; dataZ = null; dataTimeOrdered = null; dataBvac = null; dataBvacTime = null; dataIp = null; //invalidate

		if(diag == null || pulse < 0){
			return;
		}

		AUGSignalDesc desc;
		AUGSignal sig;

		int idx = diag.indexOf('#');
		String seqStr="";
		String diagOnly = diag;
		if(idx >= 0){
			seqStr = diag.substring(idx);
			diagOnly = diag.substring(0, idx);
		}

		// The PFM signal is usually too big to read over MDS+
		//SignalFetcher amds = ServiceManager.getInstance().getAUGMDSFetcher();
		AugShotfileFetcher amds = AugShotfileFetcher.defaultInstance();

		desc = new AUGSignalDesc(pulse, diagOnly, "time" +seqStr, exp);
		sig = (AUGSignal)amds.getSig(desc);
		double timeUnordered[] = (double [])sig.getDataAsType(double.class);

		try{
			desc = new AUGSignalDesc(pulse, diagOnly, "ixti"+seqStr, exp);
			sig = (AUGSignal)amds.getSig(desc);
			indexInUnordered = (int [])sig.getDataAsType(int.class);

			dataTimeOrdered = new double[indexInUnordered.length];
			for(int j=0; j < indexInUnordered.length; j++){
				indexInUnordered[j]--; //just to be annoying, that's 1 based in the shotfile
				dataTimeOrdered[j] = timeUnordered[indexInUnordered[j]];
			}

		}catch(RuntimeException err){
			System.err.println("Couldn't read " + desc + ", because... ");
			err.printStackTrace();
			System.err.println("ordering time here...");

			try{
				//reorder the time series
				indexInUnordered = new int[timeUnordered.length];
				Algorithms.quicksort(timeUnordered, indexInUnordered);
				dataTimeOrdered = timeUnordered;
				timeUnordered = null;
			}catch(RuntimeException err2){
				//ok, try the  slow way

				/*System.err.println("Couldn't sort equi timebase because... ");
				err.printStackTrace();
				System.err.println("ordering the slow way...");

				algorithms.
				java.util.Collections.sort(list);*/
				throw err2;
			}

		}

		Object o;

		desc = new AUGSignalDesc(pulse, diagOnly, "Ri"+seqStr, exp);
		sig = (AUGSignal)amds.getSig(desc);
		o = sig.getDataAsType(double.class);
		dataR = (o instanceof double[][][] array3d)  ? array3d[0] : ((double[][])o);

		desc = new AUGSignalDesc(pulse, diagOnly, "Zj"+seqStr, exp);
		sig = (AUGSignal)amds.getSig(desc);
		o = sig.getDataAsType(double.class);
		dataZ = (o instanceof double[][][] array3d)  ? array3d[0] : ((double[][])o);


		try{
			desc = new AUGSignalDesc(pulse, diagOnly, "IpiPSI"+seqStr, exp);
			AUGSignal IpSig = (AUGSignal)amds.getSig(desc);
			o = IpSig.getDataAsType(double.class);
			while(!(o instanceof double[][])) ///yea, this is really annoying
				o = ((Object[])o)[0];
			o = Mat.transpose((double[][])o);
			dataIp = ((double[][])o)[0];

		}catch(RuntimeException err){
			System.err.println("Couldn't read " + desc + ", because... ");
			err.printStackTrace();

			System.err.println("Interpolating from signal instead");

			desc = new AUGSignalDesc(pulse, "MAG", "Ipa");
			AUGSignal IpSig = (AUGSignal)amds.getSig(desc);
			double IpT[] = (double[])IpSig.getCoordsAsType(0, double.class);
			double IpV[] = (double[])IpSig.getDataAsType(double.class);
			Interpolation1D ipInterp = new Interpolation1D(IpT, IpV, InterpolationMode.LINEAR, ExtrapolationMode.CONSTANT, 0);
			dataIp = ipInterp.eval(dataTimeOrdered);
		}

		desc = new AUGSignalDesc("/aug/" + pulse + "/" + bvacSignalPath);
		AUGSignal BVACSig = (AUGSignal)amds.getSig(desc);
		dataBvac = (double[])BVACSig.getDataAsType(double.class);
		dataBvacTime = (double[])BVACSig.getTVectorAsType(double.class);

		desc = new AUGSignalDesc(pulse, diagOnly, "PFxx"+seqStr, exp);
		sig = (AUGSignal)amds.getSig(desc);
		o = sig.getDataAsType(double.class);
		dataPsiNorms = (o instanceof double[][][]) ? ((double [][][])o)[0] : ((double[][])o);

		desc = new AUGSignalDesc(pulse, diagOnly, "PFL"+seqStr, exp);
		sig = (AUGSignal)amds.getSig(desc);
		o = sig.getDataAsType(double.class);
		dataPsiLabel = (o instanceof double[][][])  ? ((double [][][])o)[0] : ((double[][])o);

		//and finally, the big one - psiN
		desc = new AUGSignalDesc(pulse, diagOnly, "PFM"+seqStr, exp, 0, true);
		System.out.print("Loading AUG Psi Map from signal '" + desc + "'... ");
		sig = (AUGSignal)amds.getSig(desc); //This can take a while, even from disk
		System.out.println("Done");
		dataPsi = (double [][][])sig.getDataAsType(double.class);

		dataJPol = null;

		this.pulse = pulse;
		this.diag = diag;
		this.exp = exp;
	}

	public double[] getPsiLabels(int timeIndex){ return dataPsiLabel[timeIndex]; }

	public Interpolation1D getFProfile(int timeIndex){
		//        Jpol = (-5.e4 * gfun * cv['BZXR'][:]).T

		//
		//gf = ds.variables['GFUN'][:];
		//fVac = ds.variables['BZXR'][:] * -0.01;
		//f = fVac[:,newaxis] * gf;
		//gf = -f * 100 / bzxr

		//f0 = bzxr * -0.01
		//f = f0 * gf <-- this is in Tesla.m

		//jpol = -5e4 * gf * bzxr
		//jpol = 2pi/mu_0 * f


		if(dataJPol == null){
			int idx = diag.indexOf('#');
			String diagOnly = this.diag;
			String seqStr="";
			if(idx >= 0){
				seqStr = diag.substring(idx);
				diagOnly = diag.substring(0, idx);
			}

			AugShotfileFetcher amds = AugShotfileFetcher.defaultInstance();
			AUGSignalDesc desc = new AUGSignalDesc(pulse, diagOnly, "Jpol"+seqStr, exp);
			AUGSignal sig = (AUGSignal)amds.getSig(desc);
			Object o = sig.getDataAsType(double.class);
			dataJPol = (o instanceof double[][][])  ? ((double [][][])o)[0] : ((double[][])o);

		}

		double f[] = new double[dataPsiLabel[timeIndex].length];

		for(int i=0; i < dataPsiLabel[timeIndex].length; i++){
			f[i] = dataJPol[timeIndex][2*i] * mu0 / 2 / Math.PI;
		}

		return new Interpolation1D(dataPsiLabel[timeIndex], f, InterpolationMode.LINEAR, ExtrapolationMode.CONSTANT_END);
	}

	public String getDiag() { return diag; }
	public String getExp() { return exp; }
	public int getPulse() { return pulse; }

	public boolean isLoaded(){ return dataPsi != null; }

	public double[] getTimeOrdered() { return dataTimeOrdered; }

	public double getEarliestTime() { return dataTimeOrdered[0]; }
	public double getLatestTime() { return dataTimeOrdered[dataTimeOrdered.length-1]; }

	public double[] getGridR(int timeIndex) { return dataR[indexInUnordered[timeIndex]]; }
	public double[] getGridZ(int timeIndex) { return dataZ[indexInUnordered[timeIndex]]; }

	public double getPsiAxis(int timeIndex) { return dataPsiNorms[indexInUnordered[timeIndex]][0]; }
	public double getPsiSeparatrix(int timeIndex) { return dataPsiNorms[indexInUnordered[timeIndex]][1]; }
	public double getPsiLimiter(int timeIndex) { return dataPsiNorms[indexInUnordered[timeIndex]][2]; }
	public double getPsiLCFS(int timeIndex) { return (dataPsiNorms[indexInUnordered[timeIndex]][1] > -1000) ? dataPsiNorms[indexInUnordered[timeIndex]][1] : dataPsiNorms[indexInUnordered[timeIndex]][2]; }

	public double getBVac0(double time) {
		int iB = Mat.getNearestIndex(dataBvacTime, time);
		return dataBvac[iB] * bvacMeasurementLocation;
	}


	public Interpolation2D createPsiInterpolator(int timeIndex) {
		return createPsiInterpolator(timeIndex, false, false);
	}

	public Interpolation2D createPsiInterpolator(int timeIndex, boolean continuous2ndDiff) {
		return createPsiInterpolator(timeIndex, false, continuous2ndDiff);
	}

	public Interpolation2D createPsiNInterpolator(int timeIndex) {
		return createPsiInterpolator(timeIndex, true, false);
	}

	public Interpolation2D createPsiNInterpolator(int timeIndex, boolean continuous2ndDiff) {
		return createPsiInterpolator(timeIndex, true, continuous2ndDiff);
	}

	public algorithmrepository.interfaces.Interpolation2D createPsiInterpolator(int timeIndex, boolean normalise, boolean continuous2ndDiff) {
		double gridR[] = dataR[indexInUnordered[timeIndex]];
		double gridZ[] = dataZ[indexInUnordered[timeIndex]];
		double gridPsi[][] = new double[gridR.length][gridZ.length];
		double psi0 = getPsiAxis(timeIndex);
		double psi1 = getPsiLCFS(timeIndex);
		for(int i=0; i < gridR.length; i++){
			for(int j=0; j < gridZ.length; j++){
				gridPsi[i][j] = dataPsi[indexInUnordered[timeIndex]][j][i];
				if(normalise)
					gridPsi[i][j] = (gridPsi[i][j]- psi0) / (psi1 - psi0);
			}
		}
		if(continuous2ndDiff) // was: new CubicInterpolationDierckx(gridR, gridZ, gridPsi), see that class
			throw new UnsupportedOperationException(DIERCKX_UNAVAILABLE);

		return new CubicInterpolation2DAdapter(gridR, gridZ, gridPsi);
	}

	public static final String DIERCKX_UNAVAILABLE = "Psi interpolation with continuous 2nd derivative (Dierckx) "
			+ "is not available yet. It needs dierckx-java, which first has to be fixed to build against current "
			+ "AlgorithmRepository and deployed to maven.minerva.cloud: see the issue at "
			+ "https://github.com/Minerva-Community/dierckx-java/issues . "
			+ "Until then, use continuous2ndDiff = false (bicubic interpolation).";

}
