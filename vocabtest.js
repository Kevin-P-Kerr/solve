var fs = require('fs');
var s = fs.readFileSync("frenchvocab.txt").toString();
var lines =  s.split("\n");

var r = Math.round(Math.random()*lines.length);

console.log(r);
setTimeout(function() { 
    console.log(lines[r]);
},15000);
