/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Controller
public class Controller_0 {

  public static final String VIDEO_SVC_PATH = "/video";
  public static final String VIDEO_DATA_PATH = VIDEO_SVC_PATH + "/{id}/data";
  private static final AtomicLong currentId = new AtomicLong(0L);

  private Map<Long, Video> video_table = new HashMap<Long, Video>();
	
	//************************** helper method **************************
	public Video save(Video entity) {
		checkAndSetId(entity);
		video_table.put(entity.getId(), entity);
		return entity;
	}
	private void checkAndSetId(Video entity) {
		if(entity.getId() == 0){
			entity.setId(currentId.incrementAndGet());
		}
	}
	private String getDataUrl(long videoId){
      String url = getUrlBaseForLocalServer() + VIDEO_SVC_PATH + "/" + videoId + "/data";
      return url;
	}
	private String getUrlBaseForLocalServer() {
	   HttpServletRequest request = 
	       ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	   String base = 
	      "http://"+request.getServerName() 
	      + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
	   return base;
	}
	//************************** helper method **************************
	
	
	/**
	 * This endpoint in the API returns a list of the videos that have
	 * been added to the server. The Video objects should be returned as
	 * JSON. 
	 * 
	 * To manually test this endpoint, run your server and open this URL in a browser:
	 * http://localhost:8080/video
	 * 
	 * @return
	 */
	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return video_table.values();
	}
	
	
	/**
	 * This endpoint allows clients to add Video objects by sending POST requests
	 * that have an application/json body containing the Video object information. 
	 * 
	 * @return
	 */
	@RequestMapping(value = VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		Video temp = save(v);
		v.setDataUrl(getDataUrl(temp.getId()));
		return temp;
	}
	
	
	/**
	 * This endpoint allows clients to set the mpeg video data for previously
	 * added Video objects by sending multipart POST requests to the server.
	 * The URL that the POST requests should be sent to includes the ID of the
	 * Video that the data should be associated with (e.g., replace {id} in
	 * the url /video/{id}/data with a valid ID of a video, such as /video/1/data
	 * -- assuming that "1" is a valid ID of a video). 
	 * 
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value = VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(
			@PathVariable("id") long id, 
			@RequestParam MultipartFile data) throws IOException {
		VideoFileManager file_manager = VideoFileManager.get();
		try {
			file_manager.saveVideoData(video_table.get(id), data.getInputStream());
		} catch (Exception e) {
			throw new ResourceNotFoundException();
		}
		return new VideoStatus(VideoStatus.VideoState.READY);
	}
	
	/**
	 * This endpoint should return the video data that has been associated with
	 * a Video object or a 404 if no video data has been set yet. The URL scheme
	 * is the same as in the method above and assumes that the client knows the ID
	 * of the Video object that it would like to retrieve video data for.
	 * 
	 * This method uses Retrofit's @Streaming annotation to indicate that the
	 * method is going to access a large stream of data (e.g., the mpeg video 
	 * data on the server). The client can access this stream of data by obtaining
	 * an InputStream from the Response as shown below:
	 * 
	 * VideoSvcApi client = ... // use retrofit to create the client
	 * Response response = client.getData(someVideoId);
	 * InputStream videoDataStream = response.getBody().in();
	 * 
	 * @param id
	 * @return
	 * @throws IOException 
	 */
	@RequestMapping(value = VIDEO_DATA_PATH, method = RequestMethod.GET)
    public void getData(@PathVariable long id, 
    					HttpServletResponse response) throws IOException {
		VideoFileManager file_manager = VideoFileManager.get();
		try {
			file_manager.copyVideoData(video_table.get(id), response.getOutputStream());
		} catch (Exception e) {
			throw new ResourceNotFoundException();
		}
	}

}