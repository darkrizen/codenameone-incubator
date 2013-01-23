document
    .getElementById('calculate')
    .addEventListener('click', function(){
        var aField = document.getElementById('input1');
        var bField = document.getElementById('input2');
        var a = parseFloat(aField.value);
        var b = parseFloat(bField.value);
        window.addAsync(a, b, function(result){
           document.getElementById('result').innerHTML = result;
        });
    }, true);

