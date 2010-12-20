function slidein(e) {
    e.stop();
    myVerticalSlide.slideIn();
}
function slideout(e){
    e.stop();
    myVerticalSlide.slideOut();
}

function init() {
    var status = {'true': 'open','false': 'close'};
    
    //-vertical

    var myVerticalSlide = new Fx.Slide('vertical_slide');

    $('v_slidein').addEvent('click', slidein);

    $('v_slideout').addEvent('click', slideout);
}

window.addEvent('domready', init);
