import com.simon.neo.NeoMap

def http = dataMap.http
def log = dataMap.log
log.info("dddaaa")
return http.get("springbootdemo/test/kingPost").body(NeoMap.of("a", 1, "b", "ok")).send();
