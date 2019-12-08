{
  mode: 'development',
  resolve: {
    modules: [
      'node_modules'
    ]
  },
  plugins: [],
  module: {
    rules: [
      {
        test: /\.js$/,
        use: [
          'kotlin-source-map-loader'
        ],
        enforce: 'pre'
      }
    ]
  },
  entry: [
    '/home/john/Downloads/MiniDrive/MiniDriveEster/build/js/packages/MiniDriveEster/kotlin/MiniDriveEster.js'
  ],
  output: {
    path: '/home/john/Downloads/MiniDrive/MiniDriveEster/build/distributions',
    filename: 'MiniDriveEster.js'
  },
  devtool: 'eval-source-map'
}