# How to
Replace the `PLACEHOLDER` in the `pom.xml` with your artifact id.
Ideally, choose a name that is a valid [module name](https://docs.oracle.com/javase/specs/jls/se21/html/jls-7.html#jls-7.7).
If you choose to not use a valid module name for your project you have to add a valid module name in your `pom.xml` like this:

```xml
<properties>
    <automatic.module.name>my.module.name</automatic.module.name>
</properties>
```

# Build
To build the project do

```bash
mvn clean package
```

A new version can be deployed by creating a new GitHub tag. The continuous integration (CI) will take care of the deployment if all checks suceed.
