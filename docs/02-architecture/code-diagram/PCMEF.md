```plantuml
@startuml

skinparam rectangle {
  BorderColor Black
  BackgroundColor<<P>>
  BackgroundColor<<C>>
  BackgroundColor<<M>> 
  BackgroundColor<<E>> 
  BackgroundColor<<F>> 
}

skinparam NoteBackgroundColor
skinparam NoteBorderColor
skinparam shadowing false
skinparam handwritten false

rectangle "Presentation (P)" <<P>> as P {
  MainFrame, InspectionDialog, ViolationsPanel, RegistryTable, ReportPreviewWindow
}

rectangle "Control (C)" <<C>> as C {
  InspectionFormController, ViolationFormController, RegistryController, ReportActionController
}

rectangle "Mediator (M)" <<M>> as M {
  InspectionMediator, ViolationMediator, ReportMediator (Оркестрация процессов и бизнес-логика)
}

rectangle "Entity (E)" <<E>> as E {
  Organization, Inspection, Violation, Status, ReportType
}

rectangle "Foundation (F)" <<F>> as F {
  db: AbstractRepository, EntityManagerFactoryUtil
  
  repository: InspectionRepo, ViolationRepo, OrgRepo
  
  report: PdfGenerator, TplLoader
  
  util: DateFormatter, ValidationUtil
}

P -[d]-> C : uses
C -[d]-> M : delegates
M -[d]-> E : manipulates
M -[d]-> F : uses services

note bottom of E
  <b>Note:</b> Строгая иерархия PCMEF.
  Каждый уровень зависит только от нижележащих.
end note

@enduml
```
