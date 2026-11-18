import { Component, input, output } from "@angular/core";
import { FormArray, FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";

@Component({
  selector: "app-ingredientes-editor",
  imports: [ReactiveFormsModule],
  templateUrl: "./ingredientes-editor.component.html",
})
export class IngredientesEditorComponent {
  // Comparte los controles del formulario padre para conservar su validación.
  readonly ingredientes = input.required<FormArray<FormGroup<{
    nombre: FormControl<string>;
    cantidad: FormControl<number>;
    unidad: FormControl<string>;
  }>>>();
  // El padre decide cómo agregar y quitar filas del formulario.
  readonly agregar = output<void>();
  readonly quitar = output<number>();
}
