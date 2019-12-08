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
    '/home/john/Documentos/MiniDrive/build/js/packages/MiniDriveEster/kotlin/MiniDriveEster.js'
  ],
  output: {
    path: '/home/john/Documentos/MiniDrive/build/distributions',
    filename: 'MiniDriveEster.js'
  },
  devtool: 'eval-source-map'
}