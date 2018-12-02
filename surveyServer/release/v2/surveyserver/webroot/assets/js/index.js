

// document ready
$(function() {
    console.log("document is ready")

    onNavMenuClick = (evt) => {
        $(window).unbind("click")

        if($(evt).hasClass("on-select")) {
            $(".menu-item.has-sub").removeClass("on-select")
        }
        else {
            $(".menu-item.has-sub").removeClass("on-select")
            $(window).click((e) => {
                console.log("asd")
                if(!(evt).contains(e.target)) {
                    $(".menu-item.has-sub").removeClass("on-select")
                    $(window).unbind("click")
                }
            })
            $(evt).toggleClass("on-select")
        }
    }

    $("#responsive-nav-button").click((e) => {
        $("#responsive-nav-button").toggleClass("is-active")
        $("#responsive-nav-list").toggleClass("is-open")
    })

    validatePassword = (p) => {
        var validate = new RegExp("^(((?=.*[a-z])(?=.*[0-9])))(?=.{6,})")
        if(validate.test(p)) {
            return true
        }
        else {
            return `<div>
                <p>Mật khẩu của bạn không đủ bảo mật</p>
                <p>Mật khẩu phải bao gồm kí tự chữ và số, chiều dài tối thiểu là 6</p>
            </div>`
        }
    }
})

$(window).scroll(function(){
    if ($(window).scrollTop() >= 70) {
        $('nav.go-nav').addClass('fixed-nav');
    }
    else {
        $('nav.go-nav').removeClass('fixed-nav');
    }
})