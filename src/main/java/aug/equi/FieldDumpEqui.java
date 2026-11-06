package aug.equi;

import java.io.IOException;

import algorithmrepository.Interpolation1D;
import algorithmrepository.Mat;
import algorithmrepository.interfaces.Interpolation2D;
import descriptors.gmds.GMDSSignalDesc;
import oneLiners.OneLiners;
import signals.gmds.GMDSSignal;

//dump field from CLISTE etc
public class FieldDumpEqui {

	public static String outPath = "/tmp/fieldForDoro";

	public static void main(String[] args) {
		int pulse = 28903;

		OneLiners.mkdir(outPath);

		AUGEquilibriumData equi = new AUGEquilibriumData();
		equi.loadData("AUGD", "EQH", pulse);

		double[] tEqui = equi.getTimeOrdered();
		double[] t = Mat.linspace(0.0, 8.0, 0.020);
		double[] R = Mat.linspace(1.0, 2.3, 0.01);
		double[] Z = Mat.linspace(-1.3, 1.3, 0.01);

		double[] dPsi = new double[3];

		int poloidalFieldSign = +1;
		boolean continuousCurrent = false;

		double[][][] BR = new double[t.length][Z.length][R.length];
		double[][][] BZ = new double[t.length][Z.length][R.length];
		double[][][] Bphi = new double[t.length][Z.length][R.length];
		double[][][] psiN = new double[t.length][Z.length][R.length];

		for(int iT=0; iT < t.length; iT++){
		//int iT=t.length/2; {
			int iTE = Mat.getNearestIndex(tEqui, t[iT]);
			System.out.println("t = " + t[iT] + ", te = " + iTE);

			for(int iZ=0;iZ < Z.length; iZ++){

				//double Bvac0 = equi.getBVac0(t[iT]);

				Interpolation2D interp = equi.createPsiInterpolator(iTE, continuousCurrent);
				Interpolation1D interpF = equi.getFProfile(iTE);
				double[][] dPsiAll = new double[R.length][2];
				double[] psiAll = interp.eval(R, Mat.fillArray(Z[iZ], R.length), dPsiAll);

				for(int iR=0; iR < R.length; iR++){
					double psi = psiAll[iR];
					dPsi[0] = dPsiAll[iR][0];
					dPsi[1] = dPsiAll[iR][1];

					double f = interpF.eval(psi);


					BR[iT][iZ][iR] = poloidalFieldSign * -(dPsi[1] / (2 * Math.PI)) / R[iR];
					//double BphiVac = Bvac0 / R[iR];
					Bphi[iT][iZ][iR] = f / R[iR];
					BZ[iT][iZ][iR] = poloidalFieldSign * (dPsi[0] / (2 * Math.PI)) / R[iR];

					psiN[iT][iZ][iR] = (psi - equi.getPsiAxis(iTE)) / (equi.getPsiLCFS(iTE) - equi.getPsiAxis(iTE));

				}
			}
		}

		double[][][][] bData = { psiN, BR, BZ, Bphi };
		GMDSSignalDesc desc = new GMDSSignalDesc(pulse, "B", "B");
		GMDSSignal s = new GMDSSignal(desc, bData, new Object[]{ t, R, Z });
		try {
			s.writeToFile(outPath + "/B-p_"+pulse);

		} catch(IOException err) {
			throw new RuntimeException(err);
		}
	}
}
