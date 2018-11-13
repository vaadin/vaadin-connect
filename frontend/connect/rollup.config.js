import resolve from 'rollup-plugin-node-resolve';
import babel from 'rollup-plugin-babel';

export default {
  input: 'index.js',
  output: {
    format: 'umd',
    file: 'dist/bundle.umd.js',
    name: 'Vaadin'
  },
  plugins: [
    resolve(),
    babel({
      presets: [
        ['@babel/preset-env', {
          targets: {
            chrome: 41,
            ie: 11,
            ios: 9,
            safari: 9
          },
          modules: false
        }]
      ]
    })
  ]
};
