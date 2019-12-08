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
  function myAlert(s) {
    window.alert(s);
  }
  function main$lambda(it) {
    var l = new URL(window.document.URL);
    if (l.searchParams.get('erro') != null)
      myAlert(toString(l.searchParams.get('erro')));
    return Unit;
  }
  function main() {
    window.addEventListener('load', main$lambda);
  }
  var package$sample = _.sample || (_.sample = {});
  package$sample.alertPass = myAlert;
  package$sample.main = main;
  main();
  Kotlin.defineModule('MiniDriveEster', _);
  return _;
}));

//# sourceMappingURL=MiniDriveEster.js.map
