package asia.zrc.springcloud.test;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "feign", url = "${request.url}")
public interface FeignClass {

    @RequestMapping(value = "/v1.0/content/querycontentbyId.do", headers = "remote=migum2.0", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String queryColumnById(@RequestParam("columnId") String columnId,
                           @RequestParam("start") String start,
                           @RequestParam("count") String count,
                           @RequestParam("needAll") String needAll,
                           @RequestParam("needStatus") String needStatus,
                           @RequestParam("needSimple") String needSimple);

    @RequestMapping(value = "/v1.0/content/singer_songs.do", headers = "remote=migum2.0", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    String singerSong(@RequestParam("singerId") String singerId,
                      @RequestParam("resourceType") String resourceType,
                      @RequestParam("pageNo") String pageNo,
                      @RequestParam("pageSize") String pageSize

    );
}
