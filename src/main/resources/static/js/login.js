layui.config({
	base : "js/"
}).use(['form','layer'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		$ = layui.jquery;
	//video背景
	$(window).resize(function(){
		if($(".video-player").width() > $(window).width()){
			$(".video-player").css({"height":$(window).height(),"width":"auto","left":-($(".video-player").width()-$(window).width())/2});
		}else{
			$(".video-player").css({"width":$(window).width(),"height":"auto","left":-($(".video-player").width()-$(window).width())/2});
		}
	}).resize();
	
	//登录按钮事件
	form.on("submit(login)",function(data){
        jQuery.ajax({
            type: 'POST',
            async: false,
            url: '/validImage',
            data: {
                code: data.field.code
            },
            success: function (result) {
                if (result.success == false) {
                    layer.alert(result.errorMessage, {icon: 1});
                } else {
                    window.location.href = "../../index.html";
                    return false;
                }
            }
        });
        return false;
    })
})
