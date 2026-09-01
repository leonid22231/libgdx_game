# Door module (test)

Decor entity with a portal trigger.

```
Entity door = DoorEntity.create(propSettings, doorSettings, col, row);
// PropModule (draw) + DoorModule (overlap → WorldController.travel)
```

`DoorModule` requires `PropModule`. Active player overlapping the door AABB travels to
`DoorSettings(targetWorldId, col, row)`.

Cooldown + edge trigger avoid instant bounce on the exit door.
