fetch("/api/apitest", (req, res) => {
    console.log(req)
    console.log(res)
}).then(r => {
    r.body.getReader().read().then(r2 => {
        let x = new TextDecoder().decode(r2.value)
        console.log(x)
    })
})
