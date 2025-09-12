-dontshrink
-ignorewarnings
-dontoptimize

# Keep main method so your app runs
-keep public class * {
    public static void main(java.lang.String[]);
}

# Keep Loader class and all its members
-keep class net.mizukilab.pit.Loader {
    *;
}