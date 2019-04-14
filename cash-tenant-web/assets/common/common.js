//获取可视区高度设置iframe高度
$(window).resize(function () {
    var h = $(window).height();
    var w = $(window).width();
    if (h > 200) {
        changeHeight();
    }
});

function changeHeight() {
    var h = $(window).height();
    var w = $(window).width();
    var iHeight = h - 50;
    var rightCont = iHeight - 66;
    $('.main-right').height(iHeight);
    $('.main-left').height(iHeight);
    $('#rightCont').height(rightCont);
    $('#container-left').outerHeight($('.main-left').height()-50);
};

$(function () {
    changeHeight();
});