package asia.zrc.springcloud.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {

    @Autowired
    private FeignClass feignClass;

    @RequestMapping(value = "/v1.0/content", method = RequestMethod.GET)
    public String queryContentById(String columnId) {
        return feignClass.queryColumnById("15030366", null, null, null, null, null);
    }


    @RequestMapping(value = "/v1.0/singer/{singerId}", method = RequestMethod.GET)
    public String singerSong(@PathVariable String singerId) {
        return feignClass.singerSong(singerId, "2|D|2003", null, null);
    }


}
