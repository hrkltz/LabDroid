import { Component, Input } from '@angular/core';


@Component({
  selector: 'icon',
  templateUrl: './icon.component.html',
  styleUrls: ['./icon.component.scss']
})
export class IconComponent {
  @Input('name') mName: String;
}
