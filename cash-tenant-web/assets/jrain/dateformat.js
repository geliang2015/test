//字符串格式化为固定长度，默认将占位符补到右侧
//@param {string} str 源字符串
//@param {int} length 固定长度
//@param {string} place 占位符，默认为 "0"
//@param {boolean} left 是否将占位符补到字符串左侧
var fix = function(str,length,place,left){
    if (typeof str !== 'string') {
        return str;
    };
    length = length || 0;
    if (str.length >= length) {
        return str;
    };
    place = place || '0';
    var len = length - str.length,
        times = Math.floor(len / place.length),
        arr = [];
    for (var i = 0; i < times; i++) {
        arr.push(place);
    };
    if (left) {
        if (str.length + arr.length < length) {
            arr.push(place.substr(0,str.length - length));
        };
        str = arr.join('') + str;
    }else{
        str += arr.join('');
        if (str.length < length) {
            str += place.substr(0,str.length - length);
        };
    }

    return str;
};

//月份全称
var MONTHNAMES = ['January','February','March','April','May','June','July','August','September','October','November','December'];



/*
		将日期格式的对象格式化为字符串，如果不提供格式化器，将采用系统默认的Date.toString
		@see https://msdn.microsoft.com/zh-cn/library/8kb3ddd4(v=vs.100).aspx
		@warning 采用format格式化的日期字符串，如果还需要parse为日期格式，请使用完全格式化器，否则将导致解析失败
				For example:
					推荐 ：format(new Date(),'yyyy-MM-dd HH:mm:ss.fff')
					错误 ：format(new Date(),'yyyy-M-d H:m:s.f')，简化的格式化器转化出的字符串无法再被正确的parse

		格式说明符                  说明                              示例
		"d"             一个月中的某一天（1 到 31）。           6/1/2009 1:45:30 PM -> 1
                                                                6/15/2009 1:45:30 PM -> 15
        "dd"            一个月中的某一天（01 到 31）。          6/1/2009 1:45:30 PM -> 01
																6/15/2009 1:45:30 PM -> 15
		"f"             日期和时间值的十分之几秒。              6/15/2009 13:45:30.617 -> 6
																6/15/2009 13:45:30.050 -> 0
		"ff"            日期和时间值的百分之几秒。				6/15/2009 13:45:30.617 -> 61
																6/15/2009 13:45:30.005 -> 00
		"fff"     		日期和时间值的毫秒。					6/15/2009 13:45:30.617 -> 617
																6/15/2009 13:45:30.0005 -> 000
		"h"				采用 12 小时制的小时（从 1 到 12）。	6/15/2009 1:45:30 AM -> 1
																6/15/2009 1:45:30 PM -> 1
		"hh"			采用 12 小时制的小时（从 01 到 12）。	6/15/2009 1:45:30 AM -> 01
																6/15/2009 1:45:30 PM -> 01
		"H"				采用 24 小时制的小时（从 0 到 23）。	6/15/2009 1:45:30 AM -> 1
																6/15/2009 1:45:30 PM -> 13
		"HH"			采用 24 小时制的小时（从 00 到 23）。	6/15/2009 1:45:30 AM -> 01
																6/15/2009 1:45:30 PM -> 13
		"m"				分钟（0 到 59）。						6/15/2009 1:09:30 AM -> 9
																6/15/2009 1:09:30 PM -> 9
		"mm"			分钟（00 到 59）。						6/15/2009 1:09:30 AM -> 09
																6/15/2009 1:09:30 PM -> 09
		"M"				月份（1 到 12）。						6/15/2009 1:45:30 PM -> 6
		"MM"			月份（01 到 12）。						6/15/2009 1:45:30 PM -> 06
		"MMM"			月份的缩写名称。						6/15/2009 1:45:30 PM -> Jun
		"MMMM"			月份的完整名称。						6/15/2009 1:45:30 PM -> June
		"s"				秒（0 到 59）。							6/15/2009 1:45:09 PM -> 9
		"ss"			秒（00 到 59）。						6/15/2009 1:45:09 PM -> 09
		"t"				AM/PM 指示符的第一个字符。				6/15/2009 1:45:30 PM -> P
		"tt"			AM/PM 指示符。                          6/15/2009 1:45:30 PM -> PM
		"y"				年份（0 到 99）。						6/15/2009 1:45:30 PM -> 9
		"yy"			年份（00 到 99）。						1/1/1900 12:00:00 AM -> 00
																6/15/2009 1:45:30 PM -> 09
		"yyy"			年份（最少三位数字）。					1/1/0900 12:00:00 AM -> 900
																1/1/1900 12:00:00 AM -> 1900
																6/15/2009 1:45:30 PM -> 2009
		"yyyy"			由四位数字表示的年份。					6/15/2009 1:45:30 PM -> 2009
		"z"				相对于 UTC 的小时偏移量，无前导零。		6/15/2009 1:45:30 PM -07:00 -> -7
		"zz"			相对于 UTC 的小时偏移量，带有表示一		6/15/2009 1:45:30 PM -07:00 -> -07
						位数值的前导零。
		"g"				公元纪年								6/15/0600 1:45:30 PM -07:00 -> -7
																6/15/2009 1:45:30 PM -07:00 -> -21
		"gg"				公元纪年（有前导0）						6/15/0600 1:45:30 PM -07:00 -> -07
																6/15/2009 1:45:30 PM -07:00 -> -21
	*/
//@param {Date} date 要格式化的日期
//@param {string} frm 格式化的字符串占位符
var format = function(date,frm){
    if (!(date instanceof Date)) {
        return date;
    };
    if (!frm) {
        return date.toString();
    };
    var year = Math.abs(date.getFullYear()),
        n = date.getFullYear() < 0 ? '-' : '',
        offset = date.getTimezoneOffset() / 60,
        yearStr = n + fix(year.toString(),4,'0',true),
        y = parseInt(yearStr.substr(yearStr.length - 2)),
        month = date.getMonth() + 1,
        d = date.getDate(),
        H = date.getHours(),
        h = H != 12 ? H % 12 : H,
        m = date.getMinutes(),
        s = date.getSeconds(),
        f = date.getMilliseconds(),
        g = year % 100 == 0 ? (year / 100 + 1) : Math.ceil(year / 100);
    return frm.replace(/y+/g,function(match){
        var z = year < 0 ? '-' : '';
        switch(match.length){
            case 1:
                return y;
            case 2:
                return n+fix(y.toString(),2,'0',true);
            default:
                return n+fix(year.toString(),match.length,'0',true);
        }
    }).replace(/d{1,2}/g,function(match){
        if (match.length == 1) {
            return d;
        }
        return fix(d.toString(),match.length,'0',true);
    }).replace(/h{1,2}/g,function(match){
        if (match.length == 1) {
            return h;
        }
        return fix(h.toString(),match.length,'0',true);
    }).replace(/H{1,2}/g,function(match){
        if (match.length == 1) {
            return H;
        }
        return fix(H.toString(),match.length,'0',true);
    }).replace(/H{1,2}/g,function(match){
        if (match.length == 1) {
            return H;
        }
        return fix(H.toString(),match.length,'0',true);
    }).replace(/m{1,2}/g,function(match){
        if (match.length == 1) {
            return m;
        }
        return fix(m.toString(),match.length,'0',true);
    }).replace(/s{1,2}/g,function(match){
        if (match.length == 1) {
            return s;
        }
        return fix(s.toString(),match.length,'0',true);
    }).replace(/f+/g,function(match){
        var ms = f.toString();
        if (ms.length > match.length) {
            return ms.substr(0,match.length);
        };
        return fix(ms.toString(),match.length,'0',true);
    }).replace(/g{1,2}/g,function(match){
        if (match.length == 1) {
            return g;
        }
        return n+fix(g.toString(),match.length,'0',true);
    }).replace(/z{1,2}/g,function(match){
        if (match.length == 1) {
            return offset;
        }
        return (offset < 0 ? '-' : '') + fix(Math.abs(offset).toString(),match.length,'0',true);
    }).replace(/t{1,2}/g,function(match){
        if (match.length == 1) {
            return H >= 12 ? 'P' : 'A';
        }
        return H >= 12 ? 'P{#}' : 'A{#}';
    }).replace(/M{1,4}/g,function(match){
        switch(match.length){
            case 1:
                return month;
            case 2:
                return fix(month.toString(),match.length,'0',true);
            default:
                var M = MONTHNAMES[month-1];
                return match.length < 4 ? M.substr(0,3) : M;
        }
    }).replace(/\{\#\}/g,'M');
};

/*
    将字符串转换为日期格式，如果未提供格式化器，则采用系统默认的Date.parse
    格式化器见format
    @param {string} dateStr 字符串形式的日期
    @param {string} frm 自定义的日期格式化器
    @example
                日期字符串													格式化器
        '2015-11-23 16:30:25.506'									'yyyy-MM-dd HH:mm:ss.fff'

        '-03世纪 -0200年December月01日 00时00分00秒000毫秒 AM -08'	'gg世纪 yyyy年MMMM月dd日 hh时mm分ss秒fff毫秒 tt zz'

*/
var parse = function(dateStr,frm){
    if (!dateStr) {
        return null;
    };
    if (!frm) {
        return new Date(Date.parse(dateStr));
    };
    var place,
        last,
        FORMATS = 'yMdhHmsfztg',
        date = new Date(0,0,1),
        h,
        z,
        t,
        //format字符串占位符相对于日期字符串的偏移量
        //年份、世纪、时区 会出现负数，导致位移
        //月份全称长度不固定，导致位移
        p = 0,
    frm = frm.split('');
    //i <= frm.length 多取一位，在循环中完全处理掉日期替换
    for (var i = 0; i <= frm.length; i++) {
        if(frm[i]!==last){
            if (place) {
                place = place.join('');
                str = subDateStr(dateStr,place,i,p);
                p = str.p;
                set(str.str, place);
            };
            place = null;
            if (FORMATS.indexOf(frm[i]) >= 0) {
                place = [frm[i]];
            };
        }else{
            place && place.push(frm[i]);
        }
        last = frm[i];
    };

    if (h) {
        t == 'PM' && (h += 12);
        date.setHours(h);
    };

    if (z) {
        z = new Date().getTimezoneOffset() / 60 - z;
        date.setHours(date.getHours() + z);
    };

    return date;

    function subDateStr(dateStr,place,i,p){
        if (!place || !place.length) {
            return '';
        };
        var start = 0,str;
        if (place == 'MMMM') { //月份全称，长度不固定，特殊处理
            start = p + i - place.length;
            str = dateStr.substring(start,p + i - 1);
            for (var j = 0; j < MONTHNAMES.length; j++) {
                if(MONTHNAMES[j].indexOf(str) >= 0){
                    str = MONTHNAMES[j];
                    break;
                }
            };
            p+=(str.length - place.length);
        }else{
            start = p + i - place.length;
            str = dateStr.substring(start,p + i);
            if (place.indexOf('y') == 0 || place.indexOf('z') == 0 || place.indexOf('g') == 0) {
                if (str.indexOf('-') == 0) { //处理负数
                    str = dateStr.substring(start,p + i+1);
                    p++;
                };
            }
        }
        return {
            str:str || '',
            p:p
        };
    };

    function set(str,place){
        if (!place || !place.length || !str || !str.length) {
            return;
        };
        var v,c;
        if (/M{3,}/.test(place)) {
            for (var i = 0; i < MONTHNAMES.length; i++) {
                if(MONTHNAMES[i].indexOf(str) == 0){
                    v = i;
                    break;
                }
            };
            v != null && date.setMonth(v);
        }else if (place == 'yy' || place == 'y') {
            var Y = fix(new Date().getFullYear().toString(),4,'0',true);
            Y = Y.substr(0,Y.length-2);
            Y += fix(str,2,'0',true);
            v = parseInt(Y);
            date.setFullYear(v);
        }else{
            c = place.substr(0,1);
            if (c == 't') {
                t = place.length == 1 ? str + 'M' : str;
            }else{
                v = parseInt(str);
                switch(c){
                    case 'h':
                        h = v;
                        break;
                    case 'z':
                        z = v;
                        break;
                    case 'y':
                        date.setFullYear(v);
                        break;
                    case 'M':
                        date.setMonth(v-1);
                        break;
                    case 'd':
                        date.setDate(v);
                        break;
                    case 'H':
                        date.setHours(v);
                        break;
                    case 'm':
                        date.setMinutes(v);
                        break;
                    case 's':
                        date.setSeconds(v);
                        break;
                    case 'f':
                        date.setMilliseconds(v);
                        break;
                }
            }
        }
    }
};