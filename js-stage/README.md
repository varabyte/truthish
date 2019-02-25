JS files are generated when running the *jsTest* task, but they are not
actually run. We set up this stage as a place to actually load and run
the tests.

Note: It's possible there's a better way, but I generated `kotlin.js` and
`kotlin-test.js` by creating a new, dummy JS project with a stubbed main
function and always-passing test function, adding a dependency on the latest
`org.jetbrains.kotlin:kotlin-test-js` library. Then, I simplied copied the
files over to this project.
