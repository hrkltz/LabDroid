import { Component } from '@angular/core';
import { WebSocketService } from '../../../../service/web-socket.service';
import { Router, ActivatedRoute } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { first } from 'rxjs/operators';


@Component({
  selector: 'chmlnProjectConfigurationFileView',
  templateUrl: './file.view.html',
  styleUrls: ['./file.view.scss']
})
export class ConfigurationFileView {
  private mProjectName: string


  constructor(
    pActivatedRoute: ActivatedRoute,
    public mWebSocketService: WebSocketService,
    private mHttpClient: HttpClient) {
      this.mProjectName = pActivatedRoute.snapshot.paramMap.get('projectName')

      this.mWebSocketService.Project2.load(this.mProjectName)
  }


  public download(fileName: string): void {
    this.mHttpClient.get(`${window.location.origin}/api/storage`, {
      headers: new HttpHeaders({
        'm-path': `/Project:${this.mProjectName}/File:${fileName}`
      }),
      responseType: 'text'
    }).subscribe(text => {
      const a = document.createElement('a')
      const blob = new Blob([text])
      const objectUrl = URL.createObjectURL(blob)
      a.href = objectUrl
      a.download = fileName;
      a.click();
      URL.revokeObjectURL(objectUrl);
    })
    }


  public delete(fileName: string): void {
    this.mWebSocketService.File.delete(this.mProjectName, fileName)
  }
}