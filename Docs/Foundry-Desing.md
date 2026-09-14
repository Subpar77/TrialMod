# Foundry System Design

## Purpose

The Foundry system is intended to provide a physical, world-integrated method for melting, storing, transporting, and casting molten materials.

The system should feel like a structure the player actually builds and operates rather than a single machine block with an inventory screen. Its major components are:

1. **Foundry Basin** — contains molten material.
2. **Tap** — extracts molten material from a valid basin and controls whether material is allowed to flow.
3. **Channel** — transports molten material away from the basin.
4. **Mold** — receives molten material and uses it to create finished items.

The long-term flow is:

```text
Raw Material
     ↓
Foundry Basin
     ↓
    Tap
     ↓
Channel → Channel → ...
                     ↓
                    Mold
                     ↓
                Finished Item
```

The system should be built incrementally. Lava will initially be used as a test fluid, but the design should not assume that lava is the only material the Foundry can contain.

---

# 1. Foundry Basin

## 1.1 General Concept

A Foundry Basin is a player-built structure made from a valid type of **Foundry Brick**.

The bricks form a closed horizontal boundary around an interior region. That interior region is the physical storage space for molten material.

The basin is not intended to be a fixed multiblock shape. Players should be able to create different layouts as long as the structure follows the validation rules for the brick tier being used.

Example shapes:

```text
BBB

B.B

BBB
```

```text
BBBB

B..B

BBBB
```

```text
BBBBB

B...B

BBB.B

BBBBB
```

The exact outline does not matter so long as the interior is valid and fully enclosed.

---

## 1.2 Basin Depth

For the initial Foundry design, all basins are **one block deep**.

The Foundry is intended to behave more like a crucible or shallow industrial basin than a large storage tank.

Supporting deeper or vertically stacked basins is outside the initial design and can be reconsidered later if it adds meaningful gameplay.

---

## 1.3 Basin Capacity

A basin's capacity is determined by the number of enclosed interior block spaces.

Each valid interior block represents one unit of storage space.

For the initial fluid model:

```text
1 interior block ≈ 1 bucket of molten material
```

A brick tier defines the **maximum number of interior spaces**, not a required basin size.

For example, if Stone Foundry Brick supports a maximum basin capacity of four interior spaces, all of the following could be valid:

```text
BBB
B.B
BBB

Capacity: 1
```

```text
BBBB
B..B
BBBB

Capacity: 2
```

```text
BBBBB
B...B
BBBBB

Capacity: 3
```

A four-space basin would also be valid.

A basin containing more interior spaces than its brick tier allows is invalid.

---

## 1.4 Stone Foundry Brick — Initial Tier

The first Foundry Brick tier is the **Stone Foundry Brick**.

Current provisional design values:

- Maximum basin interior: **4 blocks**
- Approximate maximum safe temperature: **2800°F**
- Intended to support materials up to approximately the temperature needed to melt iron
- Requires an iron pickaxe or better to recover when mined

These values are design targets and may be adjusted during balancing.

Higher-tier Foundry Brick materials may later include materials such as:

- Blackstone
- Basalt
- Other specialized refractory materials

Higher tiers may support:

- Larger basin capacities
- Higher safe temperatures
- More demanding molten materials

---

## 1.5 Basin Validation

A valid basin must satisfy all of the following initial rules:

- The basin exists on a single horizontal Y level.
- The interior consists of one contiguous region.
- Every edge of that interior region is bounded by a compatible Foundry Brick.
- There are no gaps through which the interior can escape into the surrounding world.
- The number of discovered interior spaces is at least 1.
- The number of discovered interior spaces does not exceed the maximum capacity of the Foundry Brick tier.

The final validation algorithm should therefore discover an enclosed region rather than search for one predefined shape.

The likely validation approach is a bounded **flood-fill / region search** starting from a candidate interior position.

The validator should eventually return useful information about the basin rather than only a boolean result.

Conceptually, a successful validation result may contain information such as:

```text
Valid Basin
Interior Positions: [ ... ]
Capacity: 4
Brick Tier: Stone
```

The exact Java representation will be decided during implementation.

---

# 2. Molten Material Model

## 2.1 Real World Fluids

Molten materials should exist as **real fluids in the Minecraft world**, not merely as an invisible amount stored inside a controller block.

The visible fluid is intended to be part of the gameplay.

This means molten materials can potentially:

- Flow
- Spill from a damaged basin
- Hurt players
- Interact physically with the environment
- Visually show how full the basin is

For initial development, **vanilla lava** will be used as the test fluid.

Later, materials such as molten iron should be implemented as their own fluid types while following the same general basin rules.

---

## 2.2 Basin Storage

The physical fluid occupying the basin interior is the authoritative representation of the material currently stored there.

Initially:

```text
1 full fluid source block ≈ 1 bucket of stored material
```

Flowing fluid states may require special handling later.

A future design decision will determine whether flowing fluid counts toward extractable storage or whether only full source blocks can be consumed by the Tap.

---

## 2.3 Material Compatibility

Foundry components should have material and temperature limits.

A basin should not safely contain molten material whose required temperature exceeds the safe operating temperature of its brick tier.

The exact failure behavior remains to be designed, but possible consequences include:

- Progressive block damage
- Brick destruction after a delay
- Molten material escaping into the world
- Heat or fire hazards

The system should communicate failure physically rather than silently rejecting all unsafe operations.

---

# 3. Heat and Melting

## 3.1 Heat Source

The Foundry Basin should eventually require a heat source underneath it to raise and maintain the temperature of its contents.

The exact heat-source system is not yet defined.

Possible future heat sources might include:

- Fire
- Lava
- Fuel-burning blocks
- Dedicated furnace/burner blocks
- Higher-tier heat sources for advanced materials

---

## 3.2 Melting Raw Materials

The basin should eventually accept solid materials and melt them into their molten form when sufficient heat is available.

Possible insertion methods include:

- Player throws material into the basin
- Player directly interacts with the basin
- Hopper or other automation deposits material
- Future modded transport systems

Example:

```text
Iron Ingot
    ↓
Heated Foundry Basin
    ↓
Molten Iron
```

The melting system should eventually consider:

- Material type
- Required melting temperature
- Current basin temperature
- Basin capacity
- Existing molten material

Mixing different molten materials and alloy creation are not part of the initial implementation.

---

# 4. Tap

## 4.1 General Concept

The **Tap** is a separate block placed outside the Foundry Basin.

It does **not** replace one of the Foundry Bricks in the basin wall.

This keeps the basin physically enclosed and leaves the Tap accessible to the player.

Example:

```text
BBBBBBBB

B......B

B......B[T][C][C]

BBBBBBBB       |
               |
              [M]
```

Where:

- `B` = Foundry Brick
- `T` = Tap
- `C` = Channel
- `M` = Mold

---

## 4.2 Tap Function

The Tap acts as the interface between the basin and the transport system.

Its responsibilities should eventually include:

- Determine whether it is attached to a valid Foundry Basin
- Determine what molten material exists in that basin
- Determine whether that material can be transported safely
- Remove material from the basin
- Transfer material into a connected Channel
- Allow the player to start or stop material flow

The Tap should have an interactive state such as:

```text
OPEN
CLOSED
```

The exact transfer rate is not yet defined.

---

## 4.3 Tap Attachment

The Tap should attach externally to a Foundry Brick that belongs to a valid basin.

Conceptually:

```text
Basin Interior
      |
      B[T][C]
      |
Exterior
```

The Tap therefore needs some method of locating or identifying the basin associated with the Foundry Brick behind it.

This relationship should be designed so Channels do not need to repeatedly rediscover the entire basin.

---

# 5. Channel

## 5.1 General Concept

A **Channel Block** transports molten material from a Tap toward another Channel or a production block such as a Mold.

It behaves conceptually like a pipe or open foundry trough.

Example:

```text
Basin → Tap → Channel → Channel → Mold
```

---

## 5.2 Channel Rules

Channels should eventually:

- Connect to compatible neighboring Channels
- Accept material from a Tap or another Channel
- Pass material downstream
- Deliver material to a valid production block
- Respect material and temperature limitations

Channel maximum length is still to be determined.

---

## 5.3 Channel Material Tiers

Channels should have material tiers compatible with the Foundry Brick system.

For example:

```text
Stone Channel
Blackstone Channel
Basalt Channel
```

A Channel should not safely transport material above its maximum supported temperature.

If molten material exceeds a Channel's limit, the desired eventual behavior is physical failure rather than simply refusing the material.

Possible failure sequence:

```text
Unsafe molten material enters Channel
            ↓
Channel overheats for X seconds
            ↓
Channel breaks
            ↓
Molten material spills into the world
```

Exact timing and damage behavior remain to be determined.

---

# 6. Mold

## 6.1 General Concept

The **Mold** is the endpoint where molten material becomes a crafted or cast item.

The Mold receives molten material supplied through Channels.

Example:

```text
Molten Iron
    ↓
Tap
    ↓
Channels
    ↓
Sword Mold
    ↓
Iron Sword / Cast Sword Component
```

---

## 6.2 Mold Interaction

The player should interact with the Mold to perform or complete the casting process.

The final system may consider:

- Mold type
- Required material
- Required material amount
- Cooling time
- Player interaction
- Finished item extraction

The exact mold and crafting system is intentionally left open until the Basin and transport systems are working.
Additional Concept to consider: Either to treat the mold as a crafting block and use it as a workstation for villagers. Or create a seperate, simular, workstation block.

---

# 7. Component Responsibilities

The Foundry should remain divided into separate systems with clear responsibilities.

## Foundry Brick

Responsible for:

- Forming basin boundaries
- Defining basin tier
- Defining maximum safe temperature
- Contributing to maximum basin capacity rules

It should not directly own the molten material.

## Foundry Basin

A logical structure discovered from Foundry Bricks.

Responsible for:

- Validating enclosure
- Identifying interior positions
- Determining capacity
- Determining compatible brick tier
- Providing information about the fluid occupying its interior

The Basin does not necessarily correspond to one dedicated Basin block.

## Tap

Responsible for:

- Connecting to a Basin
- Player-controlled start/stop behavior
- Extracting molten material
- Beginning transport

## Channel

Responsible for:

- Transporting molten material
- Connecting transport components
- Enforcing transport temperature/material limits

## Mold

Responsible for:

- Receiving molten material
- Holding material required for a casting operation
- Converting supplied material into a finished result

---

# 8. Initial Implementation Scope

The first functional Foundry should remain intentionally small.

## Phase 1 — Basin Geometry

- Stone Foundry Brick
- One-block-deep basins
- Arbitrary horizontal basin shape
- 1–4 interior spaces
- Fully enclosed boundary validation
- Discover and return basin interior positions

## Phase 2 — Fluid Recognition

- Use vanilla lava as the test molten material
- Detect fluid occupying basin interior
- Distinguish valid source fluid from flowing fluid if necessary
- Determine how much extractable material the basin contains

## Phase 3 — Tap

- Add Tap block
- Attach Tap externally to a basin wall
- Find the associated basin
- Add open/closed player interaction
- Extract a test amount of lava

## Phase 4 — Channel

- Add Channel block
- Connect Channels together
- Move test material through the network
- Define maximum channel distance

## Phase 5 — Mold

- Add basic Mold block
- Receive material from Channel
- Require a specific quantity
- Produce one test cast item

## Phase 6 — Heat and Custom Molten Materials

- Add basin heating
- Add temperature state
- Add material melting
- Add custom molten fluids such as molten iron
- Add brick/channel temperature limitations
- Add failure behavior for overheating

---

# 9. Current Design Decisions

The following decisions are considered established unless later playtesting shows a strong reason to change them:

- Foundry Basins are free-form rather than fixed-shape multiblocks.
- Basins are one block deep.
- Basin capacity is a maximum interior-cell count, not an exact required size.
- Stone Foundry Brick initially supports 1–4 interior spaces.
- The Tap is a separate external interactive block.
- Channels transport material away from the Tap.
- Molds are the casting/crafting endpoint.
- Molten materials exist as actual world fluids.
- Lava is only a development/test material.
- Foundry component tiers have maximum supported temperatures.
- Unsafe material handling should eventually produce physical failure consequences.
- Basin geometry, transport, and crafting should remain separate systems.

---

# 10. Open Design Questions

These are intentionally unresolved:

- Exact Stone Foundry maximum temperature and balancing values
- Exact higher-tier Foundry Brick materials
- Whether all basin boundary bricks must be the same tier
- How mixed-tier basin walls should behave, if allowed
- Exact heat-source mechanics
- How basin temperature is calculated and stored
- How flowing fluid states contribute to stored/extractable material
- Tap extraction rate
- Channel transfer rate
- Maximum Channel length
- Channel path rules and junction behavior
- Whether Channels visually contain flowing fluid
- Exact overheating delay and failure behavior
- Custom molten-material implementation
- Alloying or mixed-material behavior
- Mold types
- Mold recipes
- Cooling/solidification mechanics
- Automation interfaces
- Whether a dedicated logical basin/controller object or BlockEntity becomes necessary later

---

# 11. Reference Layout

```text
BBBBBBBB
B......B
B......B[T][C][C]
BBBBBBBB       |
               |
              [M]
```

Where:

```text
B = Foundry Brick / Foundry Basin wall
. = Basin interior / molten-material space
T = Tap
C = Channel
M = Mold
```

The Tap remains outside the Foundry Basin wall and interacts with the Basin through the adjacent Foundry Brick.

---

## Development Principle

Implement the Foundry in small, independently testable layers.

Each system should answer one clear question before the next system is added:

```text
Is the Basin structurally valid?
        ↓
What material does it contain?
        ↓
Can the Tap extract it?
        ↓
Can Channels transport it?
        ↓
Can the Mold consume it?
        ↓
Can raw material be melted and temperature simulated?
```

Avoid introducing later systems merely to make an earlier prototype work. The Basin validator should be useful to the eventual full Foundry rather than being tied permanently to a temporary 3×3 test structure.
