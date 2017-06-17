/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.george.getreadychat;

public class UserMessage {

    private String text;
    private String name;
    private String photoUrl;
    private String nameToName;
    private String time;
    private String isReaded;

    public UserMessage() {
    }

    public UserMessage(String text, String name, String nameToName, String photoUrl, String time, String isreaded) {
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.nameToName = nameToName;
        this.time = time;
        this.isReaded = isreaded;

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameToName(){
        return nameToName;
    }

    public void setNameToName (String nameToName){
        this.nameToName = nameToName;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTime(){
        return time;
    }

    public void setTime(String time){
        this.time = time;
    }

    public String getIsReaded(){
        return isReaded;
    }

    public void setIsReaded(String isreaded){
        this.isReaded = isreaded;
    }
}
