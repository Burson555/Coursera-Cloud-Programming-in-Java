package org.magnum.mobilecloud.video;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoNotFoundException;
import org.magnum.mobilecloud.video.repository.VideoRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoController {
	
	@Autowired
	private VideoRepo repo;
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getList(){
		return Lists.newArrayList(repo.findAll());
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video postVideo(@RequestBody Video v){
		return repo.save(v);
	}
	
	// helper method
	private Video getVideo(long id) throws VideoNotFoundException {
		Video v = repo.findOne(id);
		if (v == null) throw new VideoNotFoundException();
		return v;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}", method=RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id){
		return getVideo(id);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/like", method=RequestMethod.POST)
	public void postLike(
			@PathVariable("id") long id, 
			Principal p, 
			HttpServletResponse response){
		Video v = getVideo(id);
		Set<String> video_set = v.getLikedBy();
		if (video_set.add(p.getName())) {
			v.addLike();
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		repo.save(v);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/{id}/unlike", method=RequestMethod.POST)
	public void postUnlike(
			@PathVariable("id") long id, 
			Principal p, 
			HttpServletResponse response){
		Video v = getVideo(id);
		Set<String> video_set = v.getLikedBy();
		if (video_set.remove(p.getName())) {
			v.removeLike();
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		repo.save(v);
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/search/findByName", method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findVideoByName(@RequestParam("title") String title) {
		Collection<Video> videos = repo.findByName(title);
		if (videos == null) {
			videos = Collections.emptyList();
		}
		return videos;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH+"/search/findByDurationLessThan", method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findVideoByDurationLessThan(@RequestParam("duration") long duration) {
		Collection<Video> videos = repo.findByDurationLessThan(duration);
		if (videos == null) {
			videos = Collections.emptyList();
		}
		return videos;
	}
}
