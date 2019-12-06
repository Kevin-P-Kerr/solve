var generateAllChoices = function (ar) {
    if (ar.length <= 1) {
        return [];
    }
    var i,ii;
    var ret = [];
    for (i=0,ii=ar.length;i<ii;i++) {
        var q = ar[i];
        var l,ll;
        for(l=i+1,ll=ii;l<ll;l++) {
            var qq = ar[l];
            if (q.canTransmitInto(qq)) {
                var an = copy(ar);
                an.splice(i,1);
                ret.push(an);
                var z = generateAllChoices(an);
                ret = ret.concat(z);
            }
            if (qq.canTransmitInto(q)) {
                an = copy(ar);
                an.splice(l,1);
                ret.push(an);
                z = generateAllChoices(an);
                ret = ret.concat(z);
            }
        }
    }
    return ret;
};



var toStr = function (ar) {
    var s = "";
    ar.forEach(function (o) {
        if (o.negate) {
            s+="~";
        }
        if (o.isForAll) {
            s+="forall ";
        }
        else {
            s+="thereis ";
        }
        s+=o.name+" ";
    });
    return s;
};

var Quantifier = function (f,n) {
    this.isForAll = f;
    this.name= n;
    this.indices = [];
    this.canTransmitInto = function (o) {
        return this.isForAll && this.indices.indexOf(o.index) >=0;
    };
    this.copy = function () {
        var q = new Quantifier(this.isForAll,this.name);
        q.indices = this.indices;
        q.index = this.index;
        return q;
    };
};

var copy = function (ar) {
    var r = [];
    ar.forEach(function (o) {
        r.push(o.copy());
    });
    return r;
};

var x = function(f,n) {
    return new Quantifier(f,n);
};

var ar = [x(true,"biz"),x(true,"foo"),x(true,"bar"),x(true,"baz")];

var init = function (ar) {
    var i =0;
    ar.forEach(function (o) {
        o.index = i;
        i+=1;
    });

    var fa = [];
    var i = 0;
    ar.forEach(function (o) {
        if (o.isForAll) {
            o.indices = [];
            var n = i-1;
            while (n >= 0) {
                o.indices.push(n);
                n--;
            }
            fa.push(o);
        }
        else {
            var l,ll;
            for (l=0,ll=fa.length;l<ll;l++) {
                var f = fa[l];
                var j,jj;
                for (j=l+1,jj=fa.length;j<jj;j++) {
                    f.indices.push(j);
                }
            }
            fa = [];
        }
        i++;
    });
    var l,ll;
    for (l=0,ll=fa.length;l<ll;l++) {
        var f = fa[l];
        var j,jj;
        for (j=l+1,jj=fa.length;j<jj;j++) {
            f.indices.push(j);
        }
    }
};

var p = function (o) {
    console.log(toStr(o));
};

debugger;
init(ar);
var ret = generateAllChoices(ar);
ret = [ar].concat(ret);
ret.forEach(function(a) { p(a); });
console.log(ret.length);
