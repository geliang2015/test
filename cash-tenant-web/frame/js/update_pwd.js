/**
 * 执行数据保存
 */
function submitForm() {
	var validFormRes = false;
    $$.form({
    	fname:"#dataForm",
    	async:false,
        able:function(){
            validFormRes = false;
        },
        success:function () {
            validFormRes = true;
        }
    });
    return validFormRes;
}

$(function(){
	$('#b_oldPwd').focus();
});