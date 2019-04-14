function StringInsertSeperator(inputStr,separator,length){
     var strLen=inputStr.length;
     var count=strLen/length;
     var result="";
     var temp;
     var start;
     var end;
     for(var i=0;i<count;i++){
         start=i*length;
         end=(i+1)*length;
         if(end>strLen){
             end=strLen;
         }
         result=result+inputStr.substring(start,end)+separator;
     }
     if(end<strLen){
         result=result+inputStr.substring(end,strLen)+separator;
     }
     return result;

}
function jsonToUrl(transParam){
    var retStr="";
    for(var o in transParam){
        retStr=retStr+o+"="+transParam[o]+"&";
    }
    return retStr;
}
function closeWin(){
    var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
    parent.layer.close(index);
}
function closeWinAndFresh(){
    var index = parent.layer.getFrameIndex(window.name); //先得到当前iframe层的索引
    // parent.location.reload(); //刷新父页面
    parent.refresh();
    parent.layer.close(index);
}
function judgeJsonIsNotBlank(obj){
    var str=JSON.stringify(obj);
    return str.length>2;
}
function notNull(id,alertStr){
    var valuestr=$("#"+id).val();
    var b=/\S/.test(valuestr);
    if(!b){

        $$.alert(alertStr+"不能为空,请重新输入!",function(index){
            layer.close(index);
            $("#"+id).focus();
        });
        return false;
    }else{
        return b;
    }
}
function isNumber(id,alertStr){
    var valuestr=$("#"+id).val();
    var b=/^[0-9]*(\.[0-9]+)?$/.test(valuestr);
    if(!b){

        $$.alert(alertStr+"格式不正确,请重新输入!",function(index){
            layer.close(index);
            $("#"+id).focus();
        });
        return false;
    }else{
        return b;
    }
}
function isIntegerZ(id,alertStr){
    var valuestr=$("#"+id).val();
    var b=/^[0-9]*[1-9][0-9]*$/.test(valuestr);
    if(!b){

        $$.alert(alertStr+"格式不正确,请重新输入!",function(index){
            layer.close(index);
            $("#"+id).focus();
        });
        return false;
    }else{
        return b;
    }
}
function isInteger(id,alertStr){
    var valuestr=$("#"+id).val();
    var b=/^-?\d+$/.test(valuestr);
    if(!b){

        $$.alert(alertStr+"格式不正确,请重新输入!",function(index){
            layer.close(index);
            $("#"+id).focus();
        });
        return false;
    }else{
        return b;
    }
}
String.prototype.replaceAll = function(s1,s2){ return this.replace(new RegExp(s1,"gm"),s2); }
