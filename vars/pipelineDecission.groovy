#!groovy

//declaring a function
def decidePipeline(Map configMap) {
    type = configMap.get("type")
    switch(type) {
        case "nodejsEKS":
            nodejsEKS(configMap)
            break
        case "javaEKS":
            javaEKS(configMap)
            break
        case "pythonEKS":
            pythonEKS(configMap)
            break               
        default:
            error "type is not matched" 
            break
    }
}