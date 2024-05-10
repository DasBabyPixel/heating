fetch("/api/apitest", (req, res) => {
    console.error(req)
    console.error(res)
}).then(r => {
    r.body.getReader().read().then(r2 => {
        let x = new TextDecoder().decode(r2.value)
        console.error(x)
    })
})
