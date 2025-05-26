static final String region(){
    return "us-east-1"
}
static final String nexusURL(){
    return "nexus.narendra.shop:8081"
}
static final String account_id(){
    return "905418111046"
}

def getAccountID(String environment){
    switch(environment) { 
        case 'dev': 
            return "905418111046"
        case 'qa':
            return "905418111046"
        case 'uat':
            return "905418111046"
        case 'pre-prod':
            return "905418111046"
        case 'prod':
            return "905418111046"
        default:
            return "nothing"
    } 
}