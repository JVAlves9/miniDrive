(function (root, factory) {
  if (typeof define === 'function' && define.amd)
    define(['exports', 'kotlin'], factory);
  else if (typeof exports === 'object')
    factory(module.exports, require('kotlin'));
  else {
    if (typeof kotlin === 'undefined') {
      throw new Error("Error loading module 'MiniDriveEster'. Its dependency 'kotlin' was not found. Please, check whether 'kotlin' is loaded prior to 'MiniDriveEster'.");
    }
    root.MiniDriveEster = factory(typeof MiniDriveEster === 'undefined' ? {} : MiniDriveEster, kotlin);
  }
}(this, function (_, Kotlin) {
  'use strict';
  var toString = Kotlin.toString;
  var Unit = Kotlin.kotlin.Unit;
  var equals = Kotlin.equals;
  function myAlert(s) {
    window.alert(s);
  }
  function main$lambda(closure$l) {
    return function (it) {
      if (closure$l.searchParams.get('erro') != null)
        myAlert(toString(closure$l.searchParams.get('erro')));
      return Unit;
    };
  }
  function main() {
    var l = new URL(window.document.URL);
    window.addEventListener('load', main$lambda(l));
    if (equals(l.pathname, '/') && l.searchParams.get('cadastro') != null)
      window.confirm('Fa\xE7a login para acessar :)');
  }
  var package$sample = _.sample || (_.sample = {});
  package$sample.alertPass = myAlert;
  package$sample.main = main;
  main();
  Kotlin.defineModule('MiniDriveEster', _);
  return _;
}));

//# sourceMappingURL=MiniDriveEster.js.map
