/*
 *    NuStarParty
 *    Copyright (C) 2025  NuStar
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package top.nustar.nustarparty.core.entity.component;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import top.nustar.nustargui.entity.NuStarMenuButton;

/**
 * @author NuStar
 * @since 2025/8/10 12:10
 */
@Getter
public abstract class AbstractTypeButton extends NuStarMenuButton {
    protected final String type;

    public AbstractTypeButton(ConfigurationSection section) {
        super(section);
        this.type = section.getString("type");
    }
}
