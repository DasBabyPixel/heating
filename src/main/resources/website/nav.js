const path = $("script[src*=nav]").attr("src")
const dir = path.replace("nav.js", "")

// $(document).ready(function () {
$.get(dir + "nav.html", function (data) {
    $("#navigation").html(data);
})
// })
