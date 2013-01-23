/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
document.getElementById('capture')
    .addEventListener('click', function(){
        camera.capture(function(url){
            if ( url == null ){
                // No image was provided
                return;
            }
            var results = document.getElementById('results');
            results.appendChild(document.createTextNode("testing"));
            var img = document.createElement('img');
            img.setAttribute('src',url);
            img.setAttribute('width', '100');
            img.setAttribute('height', '100');
            results.appendChild(img);
        })
    }, true);
