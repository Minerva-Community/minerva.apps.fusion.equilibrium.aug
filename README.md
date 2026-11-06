# minerva.apps.fusion.equilibrium.aug

ASDEX Upgrade equilibrium data and ψ(R,Z) interpolation, package `aug.equi`.

This code used to be in [DataSignals](https://github.com/Minerva-Community/DataSignals), up to version 1.6.1. It moved here because only the AUG analysis code (ImageProc-aug) uses it. The package name is unchanged, so code that uses it only needs the new dependency:

```xml
<dependency>
    <groupId>net.minerva-central</groupId>
    <artifactId>minerva.apps.fusion.equilibrium.aug</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Contents

| Class | Purpose |
|---|---|
| `AUGEquilibriumData` | Loads an AUG equilibrium (ψ on the R/Z grid, F profile, ψ on axis and at the LCFS, B_vac) from shotfiles through DataSignals, and builds ψ(R,Z) or normalised ψ interpolations |
| `CubicInterpolation2DAdapter` | Bicubic interpolation: `algorithmrepository.Interpolation2D` with `InterpolationMode.CUBIC`, behind the old `algorithmrepository.interfaces.Interpolation2D` interface |
| `CubicInterpolation2DNR` | Alternative bicubic interpolation (Numerical Recipes style) |
| `CubicInterpolationDierckx` | Dierckx spline interpolation with a continuous 2nd derivative. **Disabled for now**, see below |
| `Splining` | Spline helpers |
| `FieldDumpEqui` | Command-line tool that dumps the equilibrium field (was `utils.FieldDumpEqui` in DataSignals) |

## Interpolation history

AlgorithmRepository 1.4.11 replaced `CubicInterpolation2D` and the `algorithmrepository.dierckx` package with stubs that throw, and 1.4.27 emptied them. Since then `AUGEquilibriumData.createPsiInterpolator()` threw on both branches.

- **Bicubic** (`continuous2ndDiff = false`, the default): fixed. `CubicInterpolation2DAdapter` uses the successor class, which has the same algorithm as the old `CubicInterpolation2D`. It was checked against the old source to about 1e-13, including derivatives and NaN outside the grid. Needs AlgorithmRepository ≥ 1.4.59, which fills derivatives for points exactly on a grid knot.
- **Dierckx** (`continuous2ndDiff = true`): throws `UnsupportedOperationException` for now. The Dierckx code moved to [dierckx-java](https://github.com/Minerva-Community/dierckx-java), which doesn't build against current AlgorithmRepository yet and isn't deployed. When it is, uncomment `CubicInterpolationDierckx` and add dierckx-java to `pom.xml`. Declare it **before** AlgorithmRepository: it uses the same package, `algorithmrepository.dierckx`, as AlgorithmRepository's empty stubs, and whichever jar comes first on the classpath wins.

## Build

```bash
mvn install
```
