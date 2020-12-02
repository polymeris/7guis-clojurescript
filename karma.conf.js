module.exports = function (config) {
    config.set({
        browsers: ['ChromeHeadless'],
        basePath: 'target',
        files: ['karma-test.js'],
        frameworks: ['cljs-test'],
        plugins: ['karma-cljs-test', 'karma-chrome-launcher', 'karma-junit-reporter'],
        colors: true,
        logLevel: config.LOG_INFO,
        client: {
            args: ["shadow.test.karma.init"],
            singleRun: true
        },
        reporters: ['progress', 'junit'],
        junitReporter: {
            outputDir: "../report",
            outputFile: "karma-test-results.xml",
            useBrowserName: false
        }
    })
};